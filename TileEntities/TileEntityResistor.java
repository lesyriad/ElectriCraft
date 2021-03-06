/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ElectriCraft.TileEntities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaDyeHelper;
import Reika.ElectriCraft.Auxiliary.CurrentThrottle;
import Reika.ElectriCraft.Base.TileEntityWireComponent;
import Reika.ElectriCraft.Registry.ElectriTiles;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityResistor extends TileEntityWireComponent implements CurrentThrottle {

	private int selectedCurrent;

	/** Use these to control current limits; change by right-clicking with dye */
	private ColorBand b1 = ColorBand.BLACK;
	private ColorBand b2 = ColorBand.BLACK;
	private ColorBand b3 = ColorBand.BLACK;

	public static enum ColorBand {
		BLACK(ReikaDyeHelper.BLACK),
		BROWN(ReikaDyeHelper.BROWN),
		RED(ReikaDyeHelper.RED),
		ORANGE(ReikaDyeHelper.ORANGE),
		YELLOW(ReikaDyeHelper.YELLOW),
		GREEN(ReikaDyeHelper.LIME),
		BLUE(ReikaDyeHelper.BLUE),
		PURPLE(ReikaDyeHelper.PURPLE),
		GRAY(ReikaDyeHelper.GRAY),
		WHITE(ReikaDyeHelper.WHITE);

		public final ReikaDyeHelper renderColor;

		public static final ColorBand[] bandList = values();

		private ColorBand(ReikaDyeHelper color) {
			renderColor = color;
		}

		public static ColorBand getBandFromColor(ReikaDyeHelper color) {
			for (int i = 0; i < bandList.length; i++) {
				ColorBand b = bandList[i];
				if (b.renderColor == color)
					return b;
			}
			return null;
		}
	}

	public ReikaDyeHelper[] getBandRenderColors() {
		return new ReikaDyeHelper[]{b1.renderColor, b2.renderColor, b3.renderColor};
	}

	@Override
	public int getResistance() {
		return 0;
	}

	@Override
	public int getCurrentLimit() {
		return selectedCurrent;
	}

	public boolean setColor(ReikaDyeHelper color, int digit) {
		ColorBand band = color != null ? ColorBand.getBandFromColor(color) : null;
		if (band == null)
			return false;
		//ReikaJavaLibrary.pConsole(band);
		if (band.ordinal() > 7 && digit == 3)
			return false;
		if (digit == 1)
			b1 = band;
		if (digit == 2)
			b2 = band;
		if (digit == 3)
			b3 = band;
		selectedCurrent = this.calculateCurrentLimit(b1, b2, b3);
		if (!worldObj.isRemote && network != null)
			network.updateWires();
		return true;
	}

	private int calculateCurrentLimit(ColorBand b1, ColorBand b2, ColorBand b3) {
		int digit1 = b1.ordinal();
		int digit2 = b2.ordinal();
		int multiplier = b3.ordinal();
		int base = Integer.parseInt(String.format("%d%d", digit1, digit2));
		return base*ReikaMathLibrary.intpow2(10, multiplier);
	}

	@Override
	public ElectriTiles getMachine() {
		return ElectriTiles.RESISTOR;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public void onNetworkChanged() {

	}

	@Override
	public void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		b1 = ColorBand.bandList[NBT.getInteger("band1")];
		b2 = ColorBand.bandList[NBT.getInteger("band2")];
		b3 = ColorBand.bandList[NBT.getInteger("band3")];

		selectedCurrent = NBT.getInteger("sel");
	}

	@Override
	public void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("band1", b1.ordinal());
		NBT.setInteger("band2", b2.ordinal());
		NBT.setInteger("band3", b3.ordinal());

		NBT.setInteger("sel", selectedCurrent);
	}

	@Override
	public float getHeight() {
		return 0.75F;
	}

	@Override
	public float getWidth() {
		return 0.5F;
	}

	@Override
	public boolean canConnect() {
		return selectedCurrent > 0;
	}

	@SideOnly(Side.CLIENT)
	public void setColor(ColorBand band, int digit) {
		switch(digit) {
			case 1:
				b1 = band;
				break;
			case 2:
				b2 = band;
				break;
			case 3:
				b3 = band;
				break;
		}
	}

}
