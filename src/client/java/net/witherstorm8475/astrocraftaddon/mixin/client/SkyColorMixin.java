package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.Astrocraft;
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
            // Get current planet
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null) {
                // No planet - default to black
                cir.setReturnValue(new Vec3d(0.0, 0.0, 0.0));
                return;
            }

            // Get planet name
            String bodyId = obsBody.getID();
            String[] parts = bodyId.split("\\.");
            String planetName = parts[parts.length - 1];

            // Get sky colors config
            AtmosphericEvents.PlanetAtmosphere atmosphere = AtmosphericEvents.getAtmosphere(planetName);
            if (atmosphere == null || atmosphere.skyColors == null) {
                // No config - default to black
                cir.setReturnValue(new Vec3d(0.0, 0.0, 0.0));
                return;
            }

            // Get time of day (0.0 to 1.0)
            double worldAngle = (double)(Astrocraft.getWorldTime() - 6000L) / 24000.0;

            // Get rotation period for this planet
            PreccesingOrbit.PrecessionConfig.PrecessionData precData =
                    PreccesingOrbit.PrecessionConfig.getPrecession(planetName);
            double rotationPeriod = precData.Day != 0 ? precData.Day : 1.0;

            // Adjust time angle by rotation period
            double timeAngle = worldAngle / rotationPeriod;
            timeAngle = timeAngle - Math.floor(timeAngle); // Keep fractional part

            // Calculate sky color based on time of day
            Vec3d skyColor = calculateSkyColor(atmosphere.skyColors, timeAngle);

            cir.setReturnValue(skyColor);

        } catch (Exception e) {
            // Error - default to black
            cir.setReturnValue(new Vec3d(0.0, 0.0, 0.0));
        }
    }

    private Vec3d calculateSkyColor(AtmosphericEvents.SkyColorsConfig colors, double timeOfDay) {
        // timeOfDay: 0.0 = midnight, 0.25 = sunrise, 0.5 = noon, 0.75 = sunset, 1.0 = midnight

        int dayColor = colors.dayColor;
        int nightColor = colors.nightColor;
        int sunriseColor = colors.sunriseColor;
        int sunsetColor = colors.sunsetColor;

        Vec3d day = colorToVec(dayColor);
        Vec3d night = colorToVec(nightColor);
        Vec3d sunrise = colorToVec(sunriseColor);
        Vec3d sunset = colorToVec(sunsetColor);

        // Sunrise: 0.2 to 0.3
        if (timeOfDay >= 0.2 && timeOfDay <= 0.3) {
            double t = (timeOfDay - 0.2) / 0.1;
            return lerp(sunrise, day, t);
        }
        // Day: 0.3 to 0.7
        else if (timeOfDay > 0.3 && timeOfDay < 0.7) {
            return day;
        }
        // Sunset: 0.7 to 0.8
        else if (timeOfDay >= 0.7 && timeOfDay <= 0.8) {
            double t = (timeOfDay - 0.7) / 0.1;
            return lerp(day, sunset, t);
        }
        // Dusk to night: 0.8 to 0.9
        else if (timeOfDay > 0.8 && timeOfDay < 0.9) {
            double t = (timeOfDay - 0.8) / 0.1;
            return lerp(sunset, night, t);
        }
        // Night: 0.9 to 0.1
        else if (timeOfDay >= 0.9 || timeOfDay <= 0.1) {
            return night;
        }
        // Pre-dawn: 0.1 to 0.2
        else {
            double t = (timeOfDay - 0.1) / 0.1;
            return lerp(night, sunrise, t);
        }
    }

    private Vec3d colorToVec(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Vec3d(r, g, b);
    }

    private Vec3d lerp(Vec3d a, Vec3d b, double t) {
        return new Vec3d(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t
        );
    }
}