package me.oondanomala.eightto25;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;

@Mod(
    modid = EightTo25.NAME,
    name = EightTo25.NAME,
    version = EightTo25.VERSION,
    clientSideOnly = true,
    updateJSON = "https://raw.githubusercontent.com/Oondanomala/8to25/master/versions.json"
)
public class EightTo25 {
    public static final String NAME = "8to25";
    public static final String VERSION = "1.2.1";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!Boolean.getBoolean("eightto25.skiprfbcheck")) {
            try {
                Class.forName("com.gtnewhorizons.retrofuturabootstrap.Main", false, getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                if (!GraphicsEnvironment.isHeadless()) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ex) {
                        LOGGER.warn(ex);
                    }
                    JOptionPane.showMessageDialog(
                        null,
                        "RetroFuturaBootstrap not found, 8to25 has probably been installed incorrectly!\n" +
                        "Please see the installation guide at https://github.com/Oondanomala/8to25#installation to learn how to install the mod.\n" +
                        "If you are sure this is a mistake, add -Deightto25.skiprfbcheck to your JVM options.",
                        "8to25 has not been installed correctly",
                        JOptionPane.ERROR_MESSAGE
                    );
                }

                LOGGER.fatal("******************************************************************************************");
                LOGGER.fatal("* RetroFuturaBootstrap not found, 8to25 has probably not been installed correctly!       *");
                LOGGER.fatal("* Please see the installation guide at https://github.com/Oondanomala/8to25#installation *");
                LOGGER.fatal("* to learn how to install the mod.                                                       *");
                LOGGER.fatal("* If you are sure this is a mistake, add -Deightto25.skiprfbcheck to your JVM options.   *");
                LOGGER.fatal("******************************************************************************************");
                FMLCommonHandler.instance().exitJava(1, false);
            }
        }
    }
}
