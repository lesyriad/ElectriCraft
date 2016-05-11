/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ElectriCraft.Items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import Reika.DragonAPI.Libraries.MathSci.ReikaEngLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.ElectriCraft.Registry.ElectriBlocks;
import Reika.ElectriCraft.TileEntities.TileEntityRFBattery;

public class ItemRFBatteryPlacer extends ItemBatteryPlacer {

	public ItemRFBatteryPlacer(int tex) {
		super(tex);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List li) {
		li.add(new ItemStack(item));

		ItemStack is = new ItemStack(item);
		is.stackTagCompound = new NBTTagCompound();
		is.stackTagCompound.setLong("nrg", TileEntityRFBattery.CAPACITY);
		li.add(is);
	}

	@Override
	protected Block getPlacingBlock() {
		return ElectriBlocks.RFBATTERY.getBlockInstance();
	}

	@Override
	protected int getPlacingMeta(ItemStack is) {
		return 0;
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer ep, List li, boolean vb) {
		long e = 0;
		if (is.stackTagCompound != null) {
			e = is.stackTagCompound.getLong("nrg");
		}
		long max = TileEntityRFBattery.CAPACITY;
		String sg = ReikaEngLibrary.getSIPrefix(e);
		String sg2 = ReikaEngLibrary.getSIPrefix(max);
		double b = ReikaMathLibrary.getThousandBase(e);
		double b2 = ReikaMathLibrary.getThousandBase(max);
		li.add(String.format("Stored Energy: %.2f %sRF/%.2f %sRF", b, sg, b2, sg2));
	}

}
