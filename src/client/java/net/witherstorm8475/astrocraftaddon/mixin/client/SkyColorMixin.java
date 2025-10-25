package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.Astrocraft;
import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.witherstorm8475.astrocraftaddon.atmospheric.AtmosphericEvents;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(ClientWorld.class)
public class SkyColorMixin {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void customSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        try {
            // --- Get current planet ---
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null) {
                cir.setReturnValue(new Vec3d(0.0, 0.0, 0.0));
                return;
            }

            String bodyId = obsBody.getID();
            String[] parts = bodyId.split("\\.");
            String planetName = parts[parts.length - 1];

            AtmosphericEvents.PlanetAtmosphere atmosphere = AtmosphericEvents.getAtmosphere(planetName);
            if (atmosphere == null || atmosphere.skyColors == null) {
                cir.setReturnValue(new Vec3d(0.0, 0.0, 0.0));
                return;
            }

            // --- Time calculation ---
            long rawTicks = Astrocraft.getWorldTime();
            PreccesingOrbit.PrecessionConfig.PrecessionData precData =
                    PreccesingOrbit.PrecessionConfig.getPrecession(planetName);
            double rotationPeriod = precData.Day != 0 ? precData.Day : 1.0;

            double ticksPerPlanetDay = 24000.0 * rotationPeriod;
            double ticksInPlanetDay = rawTicks % (long) ticksPerPlanetDay;
            if (ticksInPlanetDay < 0) ticksInPlanetDay += (long) ticksPerPlanetDay;

            double timeOfDay = ticksInPlanetDay / ticksPerPlanetDay; // 0..1 (0 = midnight)

            // --- Apply longitude offset ---
            double longitude = SkyRenderer.getLongitude(); // −180 to 180
            double longitudeOffset = longitude / 360.0; // convert to fraction of day
            timeOfDay = (timeOfDay + longitudeOffset) % 1.0;
            if (timeOfDay < 0) timeOfDay += 1.0;

            Vec3d skyColor = calculateBaseSkyColor(atmosphere.skyColors, timeOfDay);
            cir.setReturnValue(skyColor);

        } catch (Exception e) {
            cir.setReturnValue(new Vec3d(0.0, 0.0, 0.0));
        }
    }

    private Vec3d calculateBaseSkyColor(AtmosphericEvents.SkyColorsConfig colors, double timeOfDay) {
        Vec3d day = colorToVec(colors.dayColor);
        Vec3d night = colorToVec(colors.nightColor);
        Vec3d sunrise = colorToVec(colors.sunriseColor);
        Vec3d sunset = colorToVec(colors.sunsetColor);

        // Smooth sunrise/sunset transitions
        if (timeOfDay >= 0.96 || timeOfDay <= 0.00) {
            double t = (timeOfDay >= 0.96) ? (timeOfDay - 0.96) / 0.04 : (timeOfDay + 0.04) / 0.04;
            return lerp(night, sunrise, clamp01(t));
        } else if (timeOfDay <= 0.49) {
            if (timeOfDay <= 0.08) {
                double t = (timeOfDay - 0.04) / 0.04;
                return lerp(sunrise, day, clamp01(t));
            }
            if (timeOfDay > 0.45) {
                double t = (timeOfDay - 0.45) / 0.035;
                return lerp(day, sunset, clamp01(t));
            }
            return day;
        } else {
            double t = (timeOfDay - 0.49) / 0.05;
            return lerp(sunset, night, clamp01(t));
        }
    }

    private Vec3d colorToVec(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Vec3d(r, g, b);
    }

    private Vec3d lerp(Vec3d a, Vec3d b, double t) {
        t = clamp01(t);
        return new Vec3d(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
    }

    private double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
