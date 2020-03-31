package dev.binclub.falcon

import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

/**
 * @author cookiedragon234 31/Mar/2020
 */
class FalconCoreMod: IFMLLoadingPlugin {
	init {
		load()
	}
	
	override fun getModContainerClass(): String? = null
	
	override fun getASMTransformerClass(): Array<String> = emptyArray()
	
	override fun getSetupClass(): String? = null
	
	override fun injectData(data: MutableMap<String, Any>?) {}
	
	override fun getAccessTransformerClass(): String? = null
}
