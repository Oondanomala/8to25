package me.oondanomala.eightto25.redirect;

import net.lenni0451.reflect.Enums;
import net.lenni0451.reflect.Fields;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@SuppressWarnings("unused") // Used from ASM
public final class Misc {
    private static final Field modifiersField;

    static {
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unfinalizeField(Field field) {
        Fields.setInt(field, modifiersField, field.getModifiers() & ~Modifier.FINAL);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // This is fine :)
    public static <T extends Enum<?>> T addEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object[] paramValues) {
        T newValue = (T) Enums.newInstance((Class) enumType, enumName, enumType.getEnumConstants().length, paramTypes, paramValues);
        Enums.addEnumInstance((Class) enumType, (Enum) newValue);
        return newValue;
    }

    // Taken from lwjgl3ify https://github.com/GTNewHorizons/lwjgl3ify/blob/a80bceaf24feb157eefbeb74ddf5f88e5061ade9/src/main/java/me/eigenraven/lwjgl3ify/redirects/JarInputStream.java
    public static JarEntry getNextJarEntrySafe(JarInputStream jis) throws IOException {
        try {
            return jis.getNextJarEntry();
        } catch (EOFException eof) {
            System.err.println("EOF caught while searching for forge patches: " + eof);
            eof.printStackTrace(System.err);
            return null;
        }
    }

    public static void patchInstructions(ListIterator<AbstractInsnNode> iterator, Predicate<AbstractInsnNode> startEatingInstruction, Predicate<AbstractInsnNode> stopEatingInstructions, Consumer<ListIterator<AbstractInsnNode>> instructionAdder) {
        boolean eatingInstructions = false;
        while (iterator.hasNext()) {
            AbstractInsnNode insn = iterator.next();
            if (startEatingInstruction.test(insn)) {
                eatingInstructions = true;
            }
            if (stopEatingInstructions.test(insn)) {
                iterator.remove();
                instructionAdder.accept(iterator);
                return;
            }
            if (eatingInstructions) {
                iterator.remove();
            }
        }
    }
}
