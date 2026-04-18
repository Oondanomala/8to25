package me.oondanomala.eightto25.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle;
import com.gtnewhorizons.retrofuturabootstrap.api.ExtensibleClassLoader;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.jar.Manifest;

import static org.objectweb.asm.Opcodes.RETURN;

public class EarlyForgePatchTransformer implements RfbClassTransformer {
    @Override
    public String id() {
        return "early-forge-patch";
    }

    @Override
    public boolean shouldTransformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest, String className, ClassNodeHandle classNode) {
        return className.equals("net.minecraftforge.fml.common.launcher.TerminalTweaker");
    }

    @Override
    public void transformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest, String className, ClassNodeHandle classNode) {
        ClassNode node = classNode.getNode();
        if (node == null) return;

        // Do not register TerminalTransformer
        for (MethodNode method : node.methods) {
            if (method.name.equals("injectIntoClassLoader")) {
                method.instructions.clear();
                method.instructions.add(new InsnNode(RETURN));
            }
        }
    }
}
