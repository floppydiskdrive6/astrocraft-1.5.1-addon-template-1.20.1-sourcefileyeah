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

            // Determine rotation period (in Earth days)
            double rotationPeriod = precData.Day;

            // If rotation period is 0, freeze the sky
            if (rotationPeriod == 0.0) {
                double tropicalAngle = PlanetManager.getTropicalAngle();
                double longitude = SkyRenderer.getLongitude();
                cir.setReturnValue(tropicalAngle + longitude);
                return;
            }

            // Get the tropical angle (where sun should be at solar noon)
            double tropicalAngle = PlanetManager.getTropicalAngle();

            // Calculate world time
            double worldTime = Astrocraft.getWorldTime();

            // Calculate ticks per full rotation
            double minecraftDayTicks = 24000.0;
            double ticksPerRotation = minecraftDayTicks * rotationPeriod;

            // Find where we are in the current rotation cycle (0 to ticksPerRotation)
            double timeInCurrentRotation = worldTime % ticksPerRotation;

            // Custom noon is at 1/4 of the rotation (like Minecraft's default worldTime 6000)
            double customNoonTime = ticksPerRotation / 4.0;

            // Calculate time offset from custom noon
            double timeFromCustomNoon = timeInCurrentRotation - customNoonTime;

            // Calculate rotation angle from custom noon
            double rotationFromNoon = (timeFromCustomNoon / ticksPerRotation) * 360.0;

            // Sky angle = tropical angle at noon + rotation since noon
            double skyAngle = tropicalAngle + rotationFromNoon;

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
            e.printStackTrace();
            // If anything fails, let original method handle it
        }
    }
}