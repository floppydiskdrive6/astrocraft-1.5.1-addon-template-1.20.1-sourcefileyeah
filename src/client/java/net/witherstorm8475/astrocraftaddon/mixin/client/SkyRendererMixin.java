package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = SkyRenderer.class, remap = false)
public class SkyRendererMixin {

    @Inject(method = "getAxialTilt", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectAxialPrecession(CallbackInfoReturnable<Double> cir) {
        try {
            // Get the observer body
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null) {
                return; // Let original method handle it
            }

            // Get body ID and extract planet name
            String bodyId = obsBody.getID();
            String[] parts = bodyId.split("\\.");
            String bodyName = parts[parts.length - 1];

            // Get precession data for this body
            PreccesingOrbit.PrecessionConfig.PrecessionData precData =
                    PreccesingOrbit.PrecessionConfig.getPrecession(bodyName);

            // If no axial precession, use the configured value
            if (precData.axialPrecessionPeriod == 0) {
                double minTilt = precData.minAxialTilt;
                cir.setReturnValue(minTilt);
                return;
            }

            // Calculate precessing axial tilt
            double time = PlanetManager.getPlanetTime();
            double precessionAngle = (time / precData.axialPrecessionPeriod) * 360.0;

            // Oscillate between min and max tilt using cosine
            double tiltRange = (precData.maxAxialTilt - precData.minAxialTilt) / 2.0;
            double avgTilt = (precData.maxAxialTilt + precData.minAxialTilt) / 2.0;
            double currentTilt = avgTilt + tiltRange * Math.cos(Math.toRadians(precessionAngle));

            cir.setReturnValue(currentTilt);

        } catch (Exception e) {
            // If anything goes wrong, let the original method handle it
            e.printStackTrace();
        }
    }
}