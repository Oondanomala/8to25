package me.oondanomala.eightto25.mixin;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

/**
 * Simple Mixin plugin to initialize MixinExtras.
 */
public class MixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
    }
}
