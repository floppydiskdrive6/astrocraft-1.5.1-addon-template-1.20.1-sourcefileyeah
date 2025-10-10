package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.planets.Body;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;

@Mixin(net.minecraft.world.World.class)
public class SkyAngleMixin {

    @Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
    private void injectCustomSkyAngle(float tickDelta, CallbackInfoReturnable<Float> cir) {
        try {
            // === 🌍 GET OBSERVER BODY THROUGH REFLECTION ===
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            java.lang.reflect.Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null) {
                // If no observer body is set, let vanilla handle it
                return;
            }

            // Extract planet name
            String planetID = obsBody.getID();
            String planetName = planetID.contains(".")
                    ? planetID.substring(planetID.lastIndexOf('.') + 1)
                    : planetID;

            // === 📅 LOAD DAY LENGTH FROM PRECESSION JSON ===
            PreccesingOrbit.PrecessionConfig.PrecessionData precData =
                    PreccesingOrbit.PrecessionConfig.getPrecession(planetName);

            double synodicDay = precData.synodicDay;
            if (synodicDay <= 0) {
                // Fallback if no synodic day is found
                return;
            }

            // Convert game ticks to planet time
            long worldTime = ((net.minecraft.world.World)(Object)this).getTimeOfDay();
            double dayFraction = (worldTime % (long)(synodicDay * 24000L)) / (synodicDay * 24000.0);

            // Add tickDelta interpolation for smoother transitions
            double angle = (dayFraction + tickDelta / (synodicDay * 24000.0)) % 1.0;
            angle = 0.5 - MathHelper.cos((float)(angle * Math.PI)) / 2.0;

            cir.setReturnValue((float) angle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
