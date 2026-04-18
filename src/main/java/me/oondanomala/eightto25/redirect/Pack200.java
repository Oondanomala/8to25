package me.oondanomala.eightto25.redirect;

import org.apache.commons.io.IOUtils;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedMap;
import java.util.jar.JarOutputStream;

/**
 * Redirection class for Java 8's {@code java.util.jar.Pack200} classes.
 * <p>
 * Taken from {@code lwjgl3ify}.
 * See <a href="https://github.com/GTNewHorizons/lwjgl3ify/blob/a80bceaf24feb157eefbeb74ddf5f88e5061ade9/src/main/java/me/eigenraven/lwjgl3ify/redirects/Pack200.java">this</a>.
 */
@SuppressWarnings("unused") // Used from ASM
public class Pack200 {

    private Pack200() {}

    public static Unpacker newUnpacker() {
        return new UnpackerImpl();
    }

    public interface Unpacker {

        String KEEP = "keep";
        String TRUE = "true";
        String FALSE = "false";
        String DEFLATE_HINT = "unpack.deflate.hint";
        String PROGRESS = "unpack.progress";

        SortedMap<String, String> properties();

        void unpack(InputStream in, JarOutputStream out) throws IOException;

        void unpack(File in, JarOutputStream out) throws IOException;

        default void addPropertyChangeListener(PropertyChangeListener listener) {}

        default void removePropertyChangeListener(PropertyChangeListener listener) {}
    }

    public static class UnpackerImpl implements Unpacker {

        public final org.apache.commons.compress.java.util.jar.Pack200.Unpacker parent;

        UnpackerImpl() {
            parent = org.apache.commons.compress.java.util.jar.Pack200.newUnpacker();
        }

        @Override
        public SortedMap<String, String> properties() {
            return parent.properties();
        }

        @Override
        public void unpack(InputStream in, JarOutputStream out) throws IOException {
            // The Apache library doesn't handle LzmaInputStreams well
            final byte[] allData = IOUtils.toByteArray(in);
            parent.unpack(new ByteArrayInputStream(allData), out);
            out.flush();
            out.close(); // Forge expects this
        }

        @Override
        public void unpack(File in, JarOutputStream out) throws IOException {
            parent.unpack(in, out);
            out.flush();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            parent.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            parent.removePropertyChangeListener(listener);
        }
    }
}
