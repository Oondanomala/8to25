package me.oondanomala.eightto25.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.ClassHeaderMetadata;
import com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle;
import com.gtnewhorizons.retrofuturabootstrap.api.ExtensibleClassLoader;
import com.gtnewhorizons.retrofuturabootstrap.api.RetroFuturaBootstrap;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.jar.Manifest;
import java.util.stream.Stream;

// Based off of lwjgl3ify's LwjglRedirectTransformer
// https://github.com/GTNewHorizons/lwjgl3ify/blob/a80bceaf24feb157eefbeb74ddf5f88e5061ade9/src/main/java/me/eigenraven/lwjgl3ify/rfb/transformers/LwjglRedirectTransformer.java
public class RedirectTransformer extends Remapper implements RfbClassTransformer {
    private static final String[] fromPrefixes = new String[]{"java/util/jar/Pack200"};
    private static final String[] toPrefixes = new String[]{"me/oondanomala/eightto25/redirect/Pack200"};

    private static final String[] EXCLUDED_PACKAGES = Stream.concat(Arrays.stream(fromPrefixes), Arrays.stream(toPrefixes))
        .map(s -> s.replace('/', '.')).toArray(String[]::new);
    private static final byte[][] PREFIX_BYTES = Arrays.stream(fromPrefixes)
        .map(s -> s.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);

    public RedirectTransformer() {
        super(RetroFuturaBootstrap.API.newestAsmVersion());
    }

    @Override
    public String id() {
        return "redirect";
    }

    @Override
    public String[] additionalExclusions() {
        return EXCLUDED_PACKAGES;
    }

    @Override
    public boolean shouldTransformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest, String className, ClassNodeHandle classNode) {
        if (!classNode.isPresent()) return false;
        return ClassHeaderMetadata.hasSubstrings(classNode.getOriginalBytes(), PREFIX_BYTES);
    }

    @Override
    public void transformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest, String className, ClassNodeHandle classNode) {
        ClassNode originalNode = classNode.getNode();
        if (originalNode == null) return;

        ClassNode remappedNode = new ClassNode();
        originalNode.accept(new ClassRemapper(remappedNode, this));
        classNode.setNode(remappedNode);
    }

    @Override
    public String map(String typeName) {
        if (typeName == null) return null;

        for (int pfx = 0; pfx < fromPrefixes.length; pfx++) {
            if (typeName.startsWith(fromPrefixes[pfx])) {
                return toPrefixes[pfx] + typeName.substring(fromPrefixes[pfx].length());
            }
        }
        return typeName;
    }
}
