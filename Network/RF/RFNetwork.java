/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ElectriCraft.Network.RF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.ElectriCraft.ElectriCraft;
import Reika.ElectriCraft.ElectriNetworkManager;
import Reika.ElectriCraft.NetworkObject;
import Reika.ElectriCraft.Auxiliary.ElectriNetworkTickEvent;
import Reika.ElectriCraft.TileEntities.TileEntityRFCable;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class RFNetwork implements NetworkObject {

	private final Collection<TileEntityRFCable> cables = new ArrayList();
	private final HashMap<WorldLocation, EnergyInteraction> endpoints = new HashMap();
	private int energy = 0;
	private int networkLimit;

	public void setIOLimit(int limit) {
		if (networkLimit != limit) {
			networkLimit = limit;
			for (TileEntityRFCable cable : cables) {
				if (cable.getRFLimit() != networkLimit) {
					cable.setRFLimit(networkLimit);
				}
			}
		}
		//ReikaJavaLibrary.pConsole("L:"+limit);
		//Thread.dumpStack();
	}

	public int getIOLimit() {
		return networkLimit;
	}

	public RFNetwork() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void tick(ElectriNetworkTickEvent evt) {
		if (!cables.isEmpty()) {
			ArrayList<EnergyInteraction> collectibles = new ArrayList();
			ArrayList<EnergyInteraction> insertibles = new ArrayList();
			int maxCanPush = energy;
			//ReikaJavaLibrary.pConsole(this.getIOLimit(), Side.SERVER);
			for (EnergyInteraction ei : endpoints.values()) {
				maxCanPush += ei.getTotalInsertible();
				if (ei.isCollectible()) {
					collectibles.add(ei);
				}
				if (ei.isInsertible()) {
					insertibles.add(ei);
				}
			}

			//ReikaJavaLibrary.pConsole(this.getIOLimit(), Side.SERVER);
			maxCanPush = Math.min(this.getIOLimit(), maxCanPush);

			for (int i = 0; i < collectibles.size() && energy < maxCanPush; i++) {
				EnergyInteraction ei = collectibles.get(i);
				int space = maxCanPush-energy;
				energy += ei.collectEnergy(space);
			}

			for (int i = 0; i < insertibles.size() && energy > 0; i++) {
				EnergyInteraction ei = insertibles.get(i);
				int add = Math.min(energy, 1+energy/insertibles.size());
				energy -= ei.addEnergy(add);
			}
		}
	}

	public void addElement(TileEntityRFCable te) {
		if (!cables.contains(te)) {
			cables.add(te);
			if (te.getRFLimit() > 0 && te.getRFLimit() != networkLimit) {
				this.setIOLimit(Math.min(te.getRFLimit(), this.getIOLimit()));
			}
		}
	}

	public void removeElement(TileEntityRFCable te) {
		cables.remove(te);
		this.rebuild();
	}

	private void rebuild() {
		ElectriCraft.logger.debug("Remapping RF network "+this);
		for (TileEntityRFCable te : cables) {
			te.findAndJoinNetwork(te.worldObj, te.xCoord, te.yCoord, te.zCoord);
		}
		this.clear(true);
	}

	public void addConnection(IEnergyHandler ih, ForgeDirection dir) {
		if (ih instanceof TileEntityRFCable)
			return;
		EnergyInteraction has = this.getInteractionFor(ih);
		if (has == null) {
			endpoints.put(new WorldLocation((TileEntity)ih), new EnergyInteraction(ih, dir));
		}
		else {
			has.addSide(dir);
		}
	}

	public void merge(RFNetwork n) {
		if (n != this) {
			ArrayList<TileEntityRFCable> li = new ArrayList();
			for (TileEntityRFCable wire : n.cables) {
				li.add(wire);
			}
			for (EnergyInteraction ei : n.endpoints.values()) {
				EnergyInteraction has = n.getInteractionFor(ei.tile);
				if (has == null) {
					endpoints.put(new WorldLocation((TileEntity)ei.tile), ei);
				}
				else {
					has.merge(ei);
				}
			}
			n.clear(false);
			for (TileEntityRFCable wire : li) {
				wire.setNetwork(this);
			}
			if (n.getIOLimit() != 0 && n.networkLimit != networkLimit)
				this.setIOLimit(Math.min(n.getIOLimit(), this.getIOLimit()));
		}
		this.updateWires();
	}

	private EnergyInteraction getInteractionFor(IEnergyHandler tile) {
		return endpoints.get(new WorldLocation((TileEntity)tile));
	}

	private void updateWires() {

	}

	private void clear(boolean clearTiles) {
		if (clearTiles) {
			for (TileEntityRFCable cable : cables) {
				cable.resetNetwork();
			}
		}

		cables.clear();
		endpoints.clear();
		energy = 0;

		ElectriNetworkManager.instance.scheduleNetworkDiscard(this);
	}

	@Override
	public String toString() {
		return cables.size()+": "+endpoints.toString();
	}

	public int drainEnergy(int maxReceive, boolean simulate) {
		maxReceive = Math.min(maxReceive, this.getIOLimit());
		int drain = Math.min(maxReceive, energy);
		if (!simulate)
			energy -= drain;
		return drain;
	}

	public int addEnergy(int maxAdd, boolean simulate) {
		//ReikaJavaLibrary.pConsole(this.getIOLimit()+"/"+energy, Side.SERVER);
		if (energy >= this.getIOLimit())
			return 0;
		maxAdd = Math.min(this.getIOLimit(), maxAdd);
		if (!simulate)
			energy += maxAdd;
		return maxAdd;
	}

	private static class EnergyInteraction {
		private final IEnergyHandler tile;
		private final ArrayList<ForgeDirection> sides = new ArrayList();

		private EnergyInteraction(IEnergyHandler ih, ForgeDirection... dirs) {
			tile = ih;
			for (int i = 0; i < dirs.length; i++) {
				this.addSide(dirs[i]);
			}
		}

		public boolean isInsertible() {
			return this.getTotalInsertible() > 0;
		}

		public boolean isCollectible() {
			return this.getTotalCollectible() > 0;
		}

		public boolean contains(IEnergyHandler tile) {
			return tile == this.tile;
		}

		public void addSide(ForgeDirection dir) {
			if (!sides.contains(dir))
				sides.add(dir);
		}

		public void merge(EnergyInteraction ei) {
			for (int i = 0; i < ei.sides.size(); i++) {
				this.addSide(ei.sides.get(i));
			}
		}

		public int collectEnergy(int max) {
			int total = 0;
			for (int i = 0; i < sides.size(); i++) {
				ForgeDirection dir = sides.get(i);
				if (tile.canConnectEnergy(dir)) {
					int collect = max-total;
					total += tile.extractEnergy(dir, collect, false);
				}
			}
			return total;
		}

		public int addEnergy(int max) {
			int total = 0;
			for (int i = 0; i < sides.size(); i++) {
				ForgeDirection dir = sides.get(i);
				if (tile.canConnectEnergy(dir)) {
					int add = max-total;
					total += tile.receiveEnergy(dir, add, false);
				}
			}
			return total;
		}

		public int getTotalCollectible() {
			if (!(tile instanceof IEnergyProvider))
				return 0;
			int total = 0;
			for (int i = 0; i < sides.size(); i++) {
				ForgeDirection dir = sides.get(i);
				if (tile.canConnectEnergy(dir)) {
					total += tile.extractEnergy(dir, Integer.MAX_VALUE, true);
				}
			}
			return total;
		}

		public int getTotalInsertible() {
			if (!(tile instanceof IEnergyReceiver))
				return 0;
			int total = 0;
			for (int i = 0; i < sides.size(); i++) {
				ForgeDirection dir = sides.get(i);
				if (tile.canConnectEnergy(dir)) {
					total += tile.receiveEnergy(dir, Integer.MAX_VALUE, true);
				}
			}
			return total;
		}

		@Override
		public String toString() {
			return tile+" @ "+sides;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof EnergyInteraction) {
				EnergyInteraction ei = (EnergyInteraction)o;
				return ei.tile.equals(tile) && ei.sides.equals(sides);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return tile.hashCode()^sides.hashCode();
		}
	}

}
