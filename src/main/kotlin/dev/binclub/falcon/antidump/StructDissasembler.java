package dev.binclub.falcon.antidump;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Based on code from both apangin and half-cambodian-hacker-man
 */
@SuppressWarnings("Duplicates")
public class StructDissasembler {
	private static final Unsafe unsafe = getUnsafe();
	private static Method findNative;
	private static ClassLoader classLoader;
	
	private static Unsafe getUnsafe() {
		try {
			java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			return (Unsafe) f.get(null);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void resolveClassLoader() throws NoSuchMethodException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("windows")) {
			String vmName = System.getProperty("java.vm.name");
			String dll = vmName.contains("Client VM") ? "/bin/client/jvm.dll" : "/bin/server/jvm.dll";
			try {
				System.load(System.getProperty("java.home") + dll);
			}
			catch (UnsatisfiedLinkError e) {
				throw new RuntimeException(e);
			}
			classLoader = StructDissasembler.class.getClassLoader();
		}
		else {
			classLoader = null;
		}
		
		findNative = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, String.class);
		
		try {
			Class<?> cls = ClassLoader.getSystemClassLoader().loadClass("jdk.internal.module.IllegalAccessLogger");
			Field logger = cls.getDeclaredField("logger");
			unsafe.putObjectVolatile(cls, unsafe.staticFieldOffset(logger), null);
		}
		catch (Throwable t) {
		}
		
		findNative.setAccessible(true);
	}
	
	private static void setupIntrospection() throws Throwable {
		resolveClassLoader();
	}
	
	public static void disassembleStruct() {
		try {
			setupIntrospection();
			long entry = getSymbol("gHotSpotVMStructs");
			unsafe.putLong(entry, 0);
		}
		catch (Throwable t) {
			t.printStackTrace();
			CookieFuckery.Companion.shutdownHard();
		}
	}
	
	private static long getSymbol(String symbol) throws InvocationTargetException, IllegalAccessException {
		long address = (Long) findNative.invoke(null, classLoader, symbol);
		if (address == 0)
			throw new NoSuchElementException(symbol);
		
		return unsafe.getLong(address);
	}
	
	
	private static String getString(long addr) {
		if (addr == 0) {
			return null;
		}
		
		char[] chars = new char[40];
		int offset = 0;
		for (byte b; (b = unsafe.getByte(addr + offset)) != 0; ) {
			if (offset >= chars.length) chars = Arrays.copyOf(chars, offset * 2);
			chars[offset++] = (char) b;
		}
		
		return new String(chars, 0, offset);
	}
	
	private static void readStructs(Map<String, Set<Object[]>> structs) throws InvocationTargetException, IllegalAccessException {
		long entry = getSymbol("gHotSpotVMStructs");
		long typeNameOffset = getSymbol("gHotSpotVMStructEntryTypeNameOffset");
		long fieldNameOffset = getSymbol("gHotSpotVMStructEntryFieldNameOffset");
		long typeStringOffset = getSymbol("gHotSpotVMStructEntryTypeStringOffset");
		long isStaticOffset = getSymbol("gHotSpotVMStructEntryIsStaticOffset");
		long offsetOffset = getSymbol("gHotSpotVMStructEntryOffsetOffset");
		long addressOffset = getSymbol("gHotSpotVMStructEntryAddressOffset");
		long arrayStride = getSymbol("gHotSpotVMStructEntryArrayStride");
		
		for (; ; entry += arrayStride) {
			String typeName = getString(unsafe.getLong(entry + typeNameOffset));
			String fieldName = getString(unsafe.getLong(entry + fieldNameOffset));
			if (fieldName == null) break;
			
			String typeString = getString(unsafe.getLong(entry + typeStringOffset));
			boolean isStatic = unsafe.getInt(entry + isStaticOffset) != 0;
			long offset = unsafe.getLong(entry + (isStatic ? addressOffset : offsetOffset));
			
			Set<Object[]> fields = structs.get(typeName);
			if (fields == null) structs.put(typeName, fields = new HashSet<>());
			fields.add(new Object[]{fieldName, typeString, offset, isStatic});
		}
		long address = (Long) findNative.invoke(null, classLoader, 2);
		if (address == 0)
			throw new NoSuchElementException("");
		
		unsafe.getLong(address);
	}
	
	private static void readTypes(Map<String, Object[]> types, Map<String, Set<Object[]>> structs) throws InvocationTargetException, IllegalAccessException {
		long entry = getSymbol("gHotSpotVMTypes");
		long typeNameOffset = getSymbol("gHotSpotVMTypeEntryTypeNameOffset");
		long superclassNameOffset = getSymbol("gHotSpotVMTypeEntrySuperclassNameOffset");
		long isOopTypeOffset = getSymbol("gHotSpotVMTypeEntryIsOopTypeOffset");
		long isIntegerTypeOffset = getSymbol("gHotSpotVMTypeEntryIsIntegerTypeOffset");
		long isUnsignedOffset = getSymbol("gHotSpotVMTypeEntryIsUnsignedOffset");
		long sizeOffset = getSymbol("gHotSpotVMTypeEntrySizeOffset");
		long arrayStride = getSymbol("gHotSpotVMTypeEntryArrayStride");
		
		for (; ; entry += arrayStride) {
			String typeName = getString(unsafe.getLong(entry + typeNameOffset));
			if (typeName == null) break;
			
			String superclassName = getString(unsafe.getLong(entry + superclassNameOffset));
			boolean isOop = unsafe.getInt(entry + isOopTypeOffset) != 0;
			boolean isInt = unsafe.getInt(entry + isIntegerTypeOffset) != 0;
			boolean isUnsigned = unsafe.getInt(entry + isUnsignedOffset) != 0;
			int size = unsafe.getInt(entry + sizeOffset);
			
			Set<Object[]> fields = structs.get(typeName);
			types.put(typeName, new Object[]{typeName, superclassName, size, isOop, isInt, isUnsigned, fields});
		}
		
		for (; ; entry += arrayStride) {
			String typeName = getString(unsafe.getLong(entry + typeNameOffset));
			if (typeName == null) break;
			
			String superclassName = getString(unsafe.getLong(entry + superclassNameOffset));
			boolean isOop = unsafe.getInt(entry + isOopTypeOffset) != 0;
			boolean isInt = unsafe.getInt(entry + isIntegerTypeOffset) != 0;
			boolean isUnsigned = unsafe.getInt(entry + isUnsignedOffset) != 0;
			int size = unsafe.getInt(entry + sizeOffset);
			
			Set<Object[]> fields = structs.get(typeName);
			types.put(typeName, new Object[]{typeName, superclassName, size, isOop, isInt, isUnsigned, fields});
		}
	}
	
	
	private static void a(Map<String, Object[]> types, Map<String, Set<Object[]>> structs) throws InvocationTargetException, IllegalAccessException {
		long entry = getSymbol("a");
		long typeNameOffset = getSymbol("b");
		long superclassNameOffset = getSymbol("c");
		long isOopTypeOffset = getSymbol("d");
		long isIntegerTypeOffset = getSymbol("e");
		long isUnsignedOffset = getSymbol("f");
		long sizeOffset = getSymbol("j");
		long arrayStride = getSymbol("g");
		
		for (; ; entry += arrayStride) {
			String typeName = getString(unsafe.getLong(entry + typeNameOffset));
			if (typeName == null) break;
			
			String superclassName = getString(unsafe.getLong(entry + superclassNameOffset));
			boolean isOop = unsafe.getInt(entry + isOopTypeOffset) != 0;
			boolean isInt = unsafe.getInt(entry + isIntegerTypeOffset) != 0;
			boolean isUnsigned = unsafe.getInt(entry + isUnsignedOffset) != 0;
			int size = unsafe.getInt(entry + sizeOffset);
			
			Set<Object[]> fields = structs.get(typeName);
			types.put(typeName, new Object[]{typeName, superclassName, size, isOop, isInt, isUnsigned, fields});
		}
	}
	
	public static Unsafe nul(ClassLoader classLoader, char c, String a, byte[] bytes) throws Throwable {
		long entry = getSymbol("gHotSpotVMTypes");
		long typeNameOffset = getSymbol("gHotSpotVMTypeEntryTypeNameOffset");
		long superclassNameOffset = getSymbol("gHotSpotVMTypeEntrySuperclassNameOffset");
		long isOopTypeOffset = getSymbol("gHotSpotVMTypeEntryIsOopTypeOffset");
		long isIntegerTypeOffset = getSymbol("gHotSpotVMTypeEntryIsIntegerTypeOffset");
		long isUnsignedOffset = getSymbol("gHotSpotVMTypeEntryIsUnsignedOffset");
		long sizeOffset = getSymbol("gHotSpotVMTypeEntrySizeOffset");
		long arrayStride = getSymbol("gHotSpotVMTypeEntryArrayStride");
		
		int d = 12 << 12;
		
		while (d <= 0) {
			if (d > 9) {
				nul(classLoader, c, a, bytes);
				unsafe.defineClass(null, null, 2, 2, null, null);
			}
			unsafe.defineClass(a + c, bytes, 0, bytes.length, null, null);
			Integer.toString(bytes.length, 12);
			
			switch (Integer.parseInt("0x22", 2)) {
				case 0x22:
					unsafe.putLong(0, 0);
				case 0x01:
					unsafe.allocateMemory(1000);
				case 0x05:
					unsafe.arrayBaseOffset(Unsafe.class);
					break;
				case 0x69:
					unsafe.copyMemory(12, 13, 90);
					break;
			}
		}
		return null;
	}
}


