package me.oondanomala.eightto25.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.PluginContext;
import com.gtnewhorizons.retrofuturabootstrap.api.RetroFuturaBootstrap;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformerHandle;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPluginHandle;
import net.lenni0451.reflect.Modules;

public class EightTo25RfbPlugin implements RfbPlugin {
    @Override
    public void onConstruction(PluginContext ctx) {
        // Open all modules to allow deep reflection in them without JVM arguments
        Modules.openBootModule();
        // Also allow native access without JVM arguments
        Modules.enableNativeAccessToAllUnnamed();
    }

    @Override
    public RfbClassTransformer[] makeEarlyTransformers() {
        return new RfbClassTransformer[]{new EarlyForgePatchTransformer()};
    }

    @Override
    public RfbClassTransformer[] makeTransformers() {
        // Disable the unsafe reflection transformer for MPK classes,
        // works around this RFB bug: https://github.com/GTNewHorizons/RetroFuturaBootstrap/pull/17.
        // MPK does not try to modify field modifiers anyway, so this is safe.
        // Has to be here (not in onConstruction) otherwise it runs before RFB's transformer is registered.
        RfbPluginHandle rfbModernJavaRfbPlugin = RetroFuturaBootstrap.API.findPluginById("rfb-modern-java");
        if (rfbModernJavaRfbPlugin != null) {
            RfbClassTransformerHandle unsafeReflectionTransformer = rfbModernJavaRfbPlugin.findTransformerById("unsafe-reflection");
            if (unsafeReflectionTransformer != null) {
                unsafeReflectionTransformer.exclusions().add("io.github.kurrycat.mpkmod");
            }
        }

        return new RfbClassTransformer[]{
            new ForgePatchTransformer(), new RedirectTransformer()
        };
    }
}
