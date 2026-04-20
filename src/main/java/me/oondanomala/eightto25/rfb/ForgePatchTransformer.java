package me.oondanomala.eightto25.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle;
import com.gtnewhorizons.retrofuturabootstrap.api.ExtensibleClassLoader;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import me.oondanomala.eightto25.redirect.Misc;
import net.lenni0451.reflect.Fields;
import net.minecraftforge.common.util.EnumHelper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;
import java.util.jar.Manifest;

import static org.objectweb.asm.Opcodes.*;

// Based off of lwjgl3ify's ForgePatchTransformer
// https://github.com/GTNewHorizons/lwjgl3ify/blob/a80bceaf24feb157eefbeb74ddf5f88e5061ade9/src/main/java/me/eigenraven/lwjgl3ify/rfb/transformers/ForgePatchTransformer.java
public class ForgePatchTransformer implements RfbClassTransformer {
    public static final String CLASS_PATCH_MANAGER = "net.minecraftforge.fml.common.patcher.ClassPatchManager";
    public static final String ENUM_HELPER = "net.minecraftforge.common.util.EnumHelper";
    public static final String OBJECT_HOLDER_REF = "net.minecraftforge.fml.common.registry.ObjectHolderRef";
    public static final String ASM_MOD_PARSER = "net.minecraftforge.fml.common.discovery.asm.ASMModParser";
    public static final String TRACING_PRINT_STREAM = "net.minecraftforge.fml.common.TracingPrintStream";

    public static final String[] PATCHED_CLASSES = new String[]{
        CLASS_PATCH_MANAGER, ENUM_HELPER, OBJECT_HOLDER_REF, ASM_MOD_PARSER, TRACING_PRINT_STREAM
    };

    @Override
    public @NotNull String id() {
        return "forge-patch";
    }

    @Override
    public boolean shouldTransformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest, String className, ClassNodeHandle classNode) {
        for (String toPatch : PATCHED_CLASSES) {
            if (toPatch.equals(className)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void transformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest, String className, ClassNodeHandle classNode) {
        if (!classNode.isPresent()) return;
        switch (className) {
            case CLASS_PATCH_MANAGER:
                tfClassPatchManager(classNode);
                break;
            case ENUM_HELPER:
                tfEnumHelper(classNode);
                break;
            case OBJECT_HOLDER_REF:
                tfObjectHolderRef(classNode);
                break;
            case ASM_MOD_PARSER:
                tfASMModParser(classNode);
                break;
            case TRACING_PRINT_STREAM:
                tfTracingPrintStream(classNode);
                break;
        }
    }

    /**
     * Fixes an infinite loop if an {@link java.io.EOFException} happens.
     */
    private void tfClassPatchManager(ClassNodeHandle handle) {
        ClassNode node = handle.getNode();
        if (node == null) return;

        for (MethodNode method : node.methods) {
            if (method.name.equals("setup") && method.instructions != null) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.owner.equals("java/util/jar/JarInputStream") && methodInsn.name.equals("getNextJarEntry")) {
                            methodInsn.setOpcode(INVOKESTATIC);
                            methodInsn.owner = Type.getInternalName(Misc.class);
                            methodInsn.name = "getNextJarEntrySafe";
                            methodInsn.desc = "(Ljava/util/jar/JarInputStream;)Ljava/util/jar/JarEntry;";
                        }
                    }
                }
            }
        }
    }

    // No clue what this is for, this doesn't seem to be an issue?
    private void tfTracingPrintStream(ClassNodeHandle handle) {
        // Add a close() override that does not close the underlying stream
        // Pack200 tries to close this stream when loading patches.
        ClassNode node = handle.getNode();
        if (node == null || node.methods == null) {
            return;
        }
        if (node.methods.stream().anyMatch(m -> "close".equals(m.name) && "()V".equals(m.desc))) {
            // Someone already added a close method
            return;
        }
        MethodVisitor mv = node.visitMethod(ACC_PUBLIC, "close", "()V", null, null);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    /**
     * Makes {@link EnumHelper}
     * work in modern Java by using the {@code Reflect} library.
     */
    private void tfEnumHelper(ClassNodeHandle handle) {
        ClassNode node = handle.getNode();
        if (node == null) return;

        for (MethodNode method : node.methods) {
            if (method.instructions == null) continue;

            for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
                AbstractInsnNode insn = it.next();
                if (insn.getOpcode() == INVOKESTATIC) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (methodInsn.owner.equals(node.name) && methodInsn.name.equals("setup")) {
                        it.remove();
                    }
                }
            }
            if (method.name.equals("setFailsafeFieldValue")) {
                InsnList instructions = method.instructions;
                instructions.clear();
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new MethodInsnNode(
                    INVOKESTATIC,
                    Type.getInternalName(Misc.class),
                    "unfinalizeField",
                    "(Ljava/lang/reflect/Field;)V", false
                ));
                instructions.add(new VarInsnNode(ALOAD, 1));
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new VarInsnNode(ALOAD, 2));
                instructions.add(new MethodInsnNode(
                    INVOKESTATIC,
                    Type.getInternalName(Fields.class),
                    "set",
                    "(Ljava/lang/Object;Ljava/lang/reflect/Field;Ljava/lang/Object;)V", false
                ));
                instructions.add(new InsnNode(RETURN));
            }
            if (method.name.equals("addEnum") && method.desc.equals("(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Enum;")) {
                method.tryCatchBlocks.clear();
                InsnList instructions = method.instructions;
                instructions.clear();
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new VarInsnNode(ALOAD, 1));
                instructions.add(new VarInsnNode(ALOAD, 2));
                instructions.add(new VarInsnNode(ALOAD, 3));
                instructions.add(new MethodInsnNode(
                    INVOKESTATIC,
                    Type.getInternalName(Misc.class),
                    "addEnum",
                    "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Enum;", false
                ));
                instructions.add(new InsnNode(ARETURN));
            }
        }
    }

    /**
     * Makes {@code ObjectHolderRef}
     * work in modern Java by using the {@code Reflect} library.
     */
    private void tfObjectHolderRef(ClassNodeHandle handle) {
        ClassNode node = handle.getNode();
        if (node == null) return;

        for (MethodNode method : node.methods) {
            if (method.instructions == null) continue;

            for (AbstractInsnNode insn : method.instructions) {
                if (insn.getOpcode() == INVOKESTATIC) {
                    MethodInsnNode methodInsn = ((MethodInsnNode) insn);
                    if (methodInsn.name.equals("makeWritable")) {
                        methodInsn.owner = Type.getInternalName(Misc.class);
                        methodInsn.name = "unfinalizeField";
                    }
                }
            }
            // Brittle, but works :)
            if (method.name.equals("apply")) {
                Misc.patchInstructions(
                    method.instructions.iterator(),
                    insn -> insn.getOpcode() == GETSTATIC && ((FieldInsnNode) insn).name.equals("newFieldAccessor"),
                    insn -> insn.getOpcode() == POP,
                    it -> {
                        it.add(new InsnNode(ACONST_NULL));
                        it.add(new VarInsnNode(ALOAD, 0));
                        it.add(new FieldInsnNode(
                            GETFIELD,
                            node.name, "field", "Ljava/lang/reflect/Field;"
                        ));
                        it.add(new VarInsnNode(ALOAD, 1));
                        it.add(new MethodInsnNode(
                            INVOKESTATIC,
                            Type.getInternalName(Fields.class),
                            "set", "(Ljava/lang/Object;Ljava/lang/reflect/Field;Ljava/lang/Object;)V", false
                        ));
                        it.add(new InsnNode(RETURN));
                    }
                );
            }
        }
    }

    /**
     * Avoid log spam by checking
     * {@code getASMSuperType() != null}.
     */
    private void tfASMModParser(ClassNodeHandle handle) {
        ClassNode node = handle.getNode();
        if (node == null) return;

        for (MethodNode method : node.methods) {
            if (method.name.equals("isBaseMod") && method.instructions != null) {
                InsnList instructions = new InsnList();
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new MethodInsnNode(
                    INVOKEVIRTUAL,
                    node.name, "getASMSuperType", "()Lorg/objectweb/asm/Type;", false
                ));
                LabelNode label = new LabelNode();
                instructions.add(new JumpInsnNode(IFNONNULL, label));
                instructions.add(new InsnNode(ICONST_0));
                instructions.add(new InsnNode(IRETURN));
                instructions.add(label);
                method.instructions.insert(instructions);
            }
        }
    }
}
