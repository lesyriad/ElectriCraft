/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ElectriCraft.Auxiliary;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.ElectriCraft.Network.WireNetwork;

public interface NetworkTile {

	public void findAndJoinNetwork(World world, int x, int y, int z);

	public WireNetwork getNetwork();

	public void setNetwork(WireNetwork n);

	public void removeFromNetwork();

	public void resetNetwork();

	public boolean isConnectable();

	public void onNetworkChanged();

	public abstract boolean canNetworkOnSide(ForgeDirection dir);

	public World getWorld();
	public int getX();
	public int getY();
	public int getZ();
}
