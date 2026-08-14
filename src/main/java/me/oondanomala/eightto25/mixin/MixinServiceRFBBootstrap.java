package me.oondanomala.eightto25.mixin;

import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapperBootstrap;

@SuppressWarnings("unused")
public class MixinServiceRFBBootstrap extends MixinServiceLaunchWrapperBootstrap {
    @Override
    public String getName() {
        return "RetroFuturaBootstrap";
    }

    @Override
    public String getServiceClassName() {
        return "me.oondanomala.eightto25.mixin.MixinServiceRFB";
    }
}
