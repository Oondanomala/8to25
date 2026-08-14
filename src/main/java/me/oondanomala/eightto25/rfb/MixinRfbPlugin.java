package me.oondanomala.eightto25.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.PluginContext;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;

public class MixinRfbPlugin implements RfbPlugin {
    @Override
    public void onConstruction(PluginContext ctx) {
        System.setProperty("mixin.bootstrapService", "me.oondanomala.eightto25.mixin.MixinServiceRFBBootstrap");
        System.setProperty("mixin.service", "me.oondanomala.eightto25.mixin.MixinServiceRFB");
    }
    // TODO:
    //  Better mixin integration (mod ID mixin config decoration)
    //  Allow mods to specify a mixin compatibility level
    //  Maybe load mixin earlier so we can mix into more classes?
}
