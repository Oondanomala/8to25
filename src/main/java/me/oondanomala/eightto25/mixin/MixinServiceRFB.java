package me.oondanomala.eightto25.mixin;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.IAdviceProvider;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

@SuppressWarnings("unused")
public class MixinServiceRFB extends MixinServiceLaunchWrapper {
    @Override
    public void init() {
        super.init();
        // Manually load the Mixin configuration in dev as it is not automatically found
        if (Launch.blackboard.get("fml.deobfuscatedEnvironment") == Boolean.TRUE) {
            Mixins.addConfiguration("8to25.mixins.json");
        }
    }

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
