package me.oondanomala.eightto25.redirect;

import net.lenni0451.reflect.Enums;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.io.EOFException;
import java.io.IOException;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static me.oondanomala.eightto25.EightTo25.LOGGER;

/**
 * Various unrelated hooks for transformed code to call,
 * to minimize the amount of manually written bytecode.
 */
@SuppressWarnings("unused") // Used from ASM
public final class TransformerHooks {
    /**
     * @see me.oondanomala.eightto25.rfb.transformers.ForgePatchTransformer#tfEnumHelper(com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle)
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // This is fine :)
    public static <T extends Enum<?>> T addEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object[] paramValues) {
        T newValue = (T) Enums.newInstance((Class) enumType, enumName, enumType.getEnumConstants().length, paramTypes, paramValues);
        Enums.addEnumInstance((Class) enumType, (Enum) newValue);
        return newValue;
    }

    /**
     * @see me.oondanomala.eightto25.rfb.transformers.ForgePatchTransformer#tfClassPatchManager(com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle)
     */
    // Taken from lwjgl3ify https://github.com/GTNewHorizons/lwjgl3ify/blob/a80bceaf24feb157eefbeb74ddf5f88e5061ade9/src/main/java/me/eigenraven/lwjgl3ify/redirects/JarInputStream.java
    public static JarEntry getNextJarEntrySafe(JarInputStream jis) throws IOException {
        try {
            return jis.getNextJarEntry();
        } catch (EOFException eof) {
            LOGGER.warn("EOF caught while searching for forge patches", eof);
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
            if (eatingInstructions) {
                if (stopEatingInstructions.test(insn)) {
                    iterator.remove();
                    instructionAdder.accept(iterator);
                    return;
                }
                iterator.remove();
            }
        }
    }
}
