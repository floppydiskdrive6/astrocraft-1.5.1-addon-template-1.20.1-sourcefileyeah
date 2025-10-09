package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.Astrocraft;
import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.config.AstrocraftConfig;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SkyRenderer.class, remap = false)
public class SkyAngleMixin {

    @Shadow
    public static AstrocraftConfig.SkyRotationOptions options;

    @Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void removeSmoothing(CallbackInfoReturnable<Double> cir) {
        double worldAngle = (double)(Astrocraft.getWorldTime() - 6000L) / 24000.0;
        double liveAngle = (double)System.currentTimeMillis() / 8.64E7 - 0.5;

        // Get fractional part (equivalent to MathHelper.fractionalPart or class_3532.method_15385)
        double timeAngle = options.liveMode ? liveAngle : worldAngle;
        timeAngle = timeAngle - Math.floor(timeAngle);

        // REMOVED: The ugly smoothing code
        // if (!options.liveMode) {
        //     timeAngle = (timeAngle * 2.0 + 0.5 - Math.cos(timeAngle * Math.PI) / 2.0) / 3.0;
        // }

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
    }
}