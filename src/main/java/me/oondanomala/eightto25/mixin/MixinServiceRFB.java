package me.oondanomala.eightto25.mixin;

import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;
import org.spongepowered.asm.service.IAdviceProvider;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

@SuppressWarnings("unused")
public class MixinServiceRFB extends MixinServiceLaunchWrapper {
    @Override
    public String getName() {
        return "RetroFuturaBootstrap";
    }

    @Override
    public CompatibilityLevel getMaxCompatibilityLevel() {
        return CompatibilityLevel.JAVA_25;
    }

    @Override
    public IAdviceProvider getAdviceProvider() {
        return (requiredCompatibility, requiredCompatibilityString) -> {
            return "Changing the compatibility version is not currently implemented. " +
                   "A compatibility version of at least " + requiredCompatibilityString + " is required.";
        };
    }
}
