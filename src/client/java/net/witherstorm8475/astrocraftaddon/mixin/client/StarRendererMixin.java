package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.StarRenderer;
import net.witherstorm8475.astrocraftaddon.atmospheric.LightPollutionSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = StarRenderer.class, remap = false)
public class StarRendererMixin {

    /**
     * Intercept the maxMagn calculation to apply light pollution
     * This reduces the magnitude limit, hiding dimmer stars
     */
    @ModifyVariable(
            method = "renderBuffer",
            at = @At("STORE"),
            ordinal = 0,
            remap = false
    )
    private static float modifyMaxMagnitude(float maxMagn) {
        // Get the effective magnitude limit based on light pollution
        float effectiveMagnitude = LightPollutionSystem.getEffectiveMagnitudeLimit();

        // Use the lower of the two values (more restrictive)
        return Math.min(maxMagn, effectiveMagnitude);
    }
}