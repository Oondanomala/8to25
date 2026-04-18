package me.oondanomala.eightto25.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.PluginContext;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;
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
        return new RfbClassTransformer[]{
            new ForgePatchTransformer(), new RedirectTransformer()
        };
    }
}
