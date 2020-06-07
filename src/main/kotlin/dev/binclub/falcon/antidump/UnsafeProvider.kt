package dev.binclub.falcon.antidump

import sun.misc.Unsafe

/**
 * @author cookiedragon234 07/Jun/2020
 */
val unsafe: Unsafe by lazy {
	Unsafe::class.java.getDeclaredField("theUnsafe").let {
		it.isAccessible = true
		it[null] as Unsafe
	}
}
