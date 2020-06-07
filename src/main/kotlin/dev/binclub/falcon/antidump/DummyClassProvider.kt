package dev.binclub.falcon.antidump

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

/**
 * @author cookiedragon234 07/Jun/2020
 */
private val dummyJavaCode: InsnList by lazy {
	InsnList().apply {
		add(FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
		add(LdcInsnNode("Nice try"))
		add(MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false))
		add(TypeInsnNode(NEW, "java/lang/Throwable"))
		add(InsnNode(DUP))
		add(LdcInsnNode("owned"))
		add(MethodInsnNode(INVOKESPECIAL, "java/lang/Throwable", "<init>", "(Ljava/lang/String;)V", false))
		add(InsnNode(ATHROW))
	}
}
fun createDummyClass(name: String): ByteArray {
	val classNode = ClassNode().apply {
		this.name = name.replace('.', '/')
		this.access = ACC_PUBLIC
		this.version = V1_8
		this.superName = "java/lang/Object"
		this.methods = arrayListOf(
			MethodNode(
				ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V", null, null
			).apply {
				this.instructions = dummyJavaCode
			}
		)
	}
	return ClassWriter(ClassWriter.COMPUTE_FRAMES).also {
		classNode.accept(it)
	}.toByteArray()
}
