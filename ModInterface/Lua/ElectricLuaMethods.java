/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ElectriCraft.ModInterface.Lua;

import Reika.DragonAPI.ModInteract.Lua.LuaMethod;

public class ElectricLuaMethods {

	private static final LuaMethod getVoltage = new LuaGetVoltage();
	private static final LuaMethod getCurrent = new LuaGetCurrent();

}
