package com.myclient;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * @author cookiedragon234 31/Mar/2020
 */
@Mod(
	modid = MyClientMod.MOD_ID,
	name = MyClientMod.MOD_NAME,
	version = MyClientMod.VERSION
)
public class MyClientMod {
	public static final String MOD_ID = "myClient";
	public static final String MOD_NAME = "My Client";
	public static final String VERSION = "0.1";
	
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		System.out.println("Mod preinit");
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("Mod init");
	}
	
	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		System.out.println("Mod postinit");
	}
}
