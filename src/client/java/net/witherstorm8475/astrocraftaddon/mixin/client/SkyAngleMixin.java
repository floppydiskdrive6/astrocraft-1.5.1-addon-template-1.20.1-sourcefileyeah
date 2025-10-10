package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.Astrocraft;
import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.config.AstrocraftConfig;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = SkyRenderer.class, remap = false)
public class SkyAngleMixin {

    @Shadow
    public static AstrocraftConfig.SkyRotationOptions options;

    @Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void applyCustomRotationPeriod(CallbackInfoReturnable<Double> cir) {
        try {
            // Get the observer body (current planet)
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null) {
                return; // Let original method handle it
            }

            // Get body name
            String bodyId = obsBody.getID();
            String[] parts = bodyId.split("\\.");
            String bodyName = parts[parts.length - 1];

            // Get precession data
            PreccesingOrbit.PrecessionConfig.PrecessionData precData =
                    PreccesingOrbit.PrecessionConfig.getPrecession(bodyName);

            // Determine rotation period - use sidereal for both planets and moons
            double rotationPeriod = precData.siderealDay != 0 ? precData.siderealDay : 1.0;

            // If rotation period is 1.0 (default), let original method handle it
            if (Math.abs(rotationPeriod - 1.0) < 0.0001) {
                return;
            }

            // Calculate sky angle with custom rotation period
            double worldAngle = (double)(Astrocraft.getWorldTime() - 6000L) / 24000.0;
            double liveAngle = (double)System.currentTimeMillis() / 8.64E7 - 0.5;

            // Get fractional part
            double timeAngle = options.liveMode ? liveAngle : worldAngle;
            timeAngle = timeAngle - Math.floor(timeAngle);

            // Apply rotation period scaling
            // Faster rotation = smaller period = more rotations per day
            timeAngle = timeAngle / rotationPeriod;
            timeAngle = timeAngle - Math.floor(timeAngle); // Keep fractional part

            double skyAngle = 360.0 * timeAngle + PlanetManager.getTropicalAngle();

            // Handle spyglass lock
            try {
                Class<?> spyglassManager = Class.forName("mod.lwhrvw.astrocraft.SpyglassManager");
                java.lang.reflect.Method isInUse = spyglassManager.getDeclaredMethod("isInUse");
                isInUse.setAccessible(true);
                boolean inUse = (boolean) isInUse.invoke(null);

                if (inUse) {
                    java.lang.reflect.Field lockField = SkyRenderer.class.getDeclaredField("spyglassAngleLock");
                    lockField.setAccessible(true);
                    double spyglassAngleLock = lockField.getDouble(null);
                    if (!Double.isNaN(spyglassAngleLock)) {
                        skyAngle = spyglassAngleLock;
                    }
                }

                // Update lock
                java.lang.reflect.Field lockField = SkyRenderer.class.getDeclaredField("spyglassAngleLock");
                lockField.setAccessible(true);
                lockField.setDouble(null, skyAngle);
            } catch (Exception e) {
                // If spyglass stuff fails, just continue
            }

            double longitude = SkyRenderer.getLongitude();
            cir.setReturnValue(skyAngle + longitude);

        } catch (Exception e) {
            // If anything fails, let original method handle it
        }
    }
}