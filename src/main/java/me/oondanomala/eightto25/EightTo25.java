package me.oondanomala.eightto25;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;

@Mod(modid = EightTo25.MODID, name = EightTo25.NAME, version = EightTo25.VERSION, clientSideOnly = true)
public class EightTo25 {
    public static final String MODID = "8to25";
    public static final String NAME = "8to25";
    public static final String VERSION = "1.0.1";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!Boolean.getBoolean("eightto25.skiprfbcheck")) {
            try {
                Class.forName("com.gtnewhorizons.retrofuturabootstrap.Main", false, getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                Logger logger = event.getModLog();
                if (!GraphicsEnvironment.isHeadless()) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ex) {
                        logger.warn(ex);
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

                logger.fatal("******************************************************************************************");
                logger.fatal("* RetroFuturaBootstrap not found, 8to25 has probably not been installed correctly!       *");
                logger.fatal("* Please see the installation guide at https://github.com/Oondanomala/8to25#installation *");
                logger.fatal("* to learn how to install the mod.                                                       *");
                logger.fatal("* If you are sure this is a mistake, add -Deightto25.skiprfbcheck to your JVM options.   *");
                logger.fatal("******************************************************************************************");
                FMLCommonHandler.instance().exitJava(1, false);
            }
        }
    }
}
