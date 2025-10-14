package net.witherstorm8475.astrocraftaddon.atmospheric;

import mod.lwhrvw.astrocraft.Astrocraft;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.lang.reflect.Field;

public class LightPollutionSystem {

    public enum Mode {
        OFF,      // No light pollution
        LOCKED,   // Fixed intensity from config
        DYNAMIC   // Based on nearby light sources in chunks
    }

    private static final int CHUNK_RADIUS = 8; // Check chunks in 8 chunk radius
    private static float cachedIntensity = 0.0f;
    private static long lastUpdateTick = 0;
    private static final int UPDATE_INTERVAL = 20; // Update every second

    /**
     * Get the current light pollution intensity (0.0 to 1.0)
     * 0.0 = no pollution, 1.0 = maximum pollution
     */
    public static float getLightPollutionIntensity() {
        try {
            // Get global light pollution config
            AtmosphericEvents.LightPollutionConfig config = AtmosphericEvents.getLightPollution();
            if (config == null) {
                return 0.0f;
            }

            Mode mode = config.mode;

            switch (mode) {
                case OFF:
                    return 0.0f;

                case LOCKED:
                    return config.intensity;

                case DYNAMIC:
                    return calculateDynamicLightPollution(config.intensity);

                default:
                    return 0.0f;
            }

        } catch (Exception e) {
            return 0.0f;
        }
    }

    /**
     * Calculate dynamic light pollution by tallying light sources in nearby chunks
     */
    private static float calculateDynamicLightPollution(float baseIntensity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return 0.0f;
        }

        // Cache results to avoid recalculating every frame
        long currentTick = client.world.getTime();
        if (currentTick - lastUpdateTick < UPDATE_INTERVAL) {
            return cachedIntensity;
        }
        lastUpdateTick = currentTick;

        World world = client.world;
        BlockPos playerPos = client.player.getBlockPos();
        ChunkPos playerChunk = new ChunkPos(playerPos);

        int totalLightSources = 0;
        int totalBrightLights = 0; // Light level >= 14
        int chunksChecked = 0;

        // Check all chunks in radius
        for (int cx = -CHUNK_RADIUS; cx <= CHUNK_RADIUS; cx++) {
            for (int cz = -CHUNK_RADIUS; cz <= CHUNK_RADIUS; cz++) {
                ChunkPos chunkPos = new ChunkPos(playerChunk.x + cx, playerChunk.z + cz);
                WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);

                if (chunk == null) continue;
                chunksChecked++;

                // Sample light levels throughout the chunk
                // Sample every 4 blocks for performance
                for (int x = 0; x < 16; x += 4) {
                    for (int z = 0; z < 16; z += 4) {
                        // Sample at multiple heights
                        for (int y = playerPos.getY() - 32; y <= playerPos.getY() + 32; y += 8) {
                            if (y < world.getBottomY() || y > world.getTopY()) continue;

                            BlockPos samplePos = new BlockPos(
                                    chunkPos.getStartX() + x,
                                    y,
                                    chunkPos.getStartZ() + z
                            );

                            int blockLight = world.getLightLevel(LightType.BLOCK, samplePos);

                            if (blockLight > 0) {
                                totalLightSources++;

                                // Very bright lights contribute more to pollution
                                if (blockLight >= 14) {
                                    totalBrightLights++;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (chunksChecked == 0) {
            cachedIntensity = 0.0f;
            return 0.0f;
        }

        // Calculate light density per chunk
        float lightDensity = (float) totalLightSources / chunksChecked;
        float brightDensity = (float) totalBrightLights / chunksChecked;

        // Normalize to 0-1 range
        // Typical outdoor area: ~10-50 light sources per chunk
        // City area: 100-500+ light sources per chunk
        float densityFactor = Math.min(1.0f, lightDensity / 200.0f);
        float brightFactor = Math.min(1.0f, brightDensity / 50.0f);

        // Weight bright lights more heavily
        float combinedFactor = densityFactor * 0.4f + brightFactor * 0.6f;

        // Apply base intensity as multiplier
        cachedIntensity = combinedFactor * baseIntensity;

        return cachedIntensity;
    }

    /**
     * Get the effective magnitude limit based on light pollution
     * LOWER magnitude limit = only show brighter stars (magnitude closer to 0 or negative)
     * Code checks: if (star.magn <= maxMagn) then render
     */
    public static float getEffectiveMagnitudeLimit() {
        float baseMagnitude = (float) Astrocraft.CONFIG.magnitudeLimit;
        float pollution = getLightPollutionIntensity();

        if (pollution <= 0.0f) {
            return baseMagnitude; // No pollution, see all stars up to base limit
        }

        // Light pollution LOWERS the magnitude limit (more restrictive)
        // At 0% pollution: base limit (e.g., 6.5 - see dim stars)
        // At 100% pollution: limit = 2.0 (only very bright stars)

        float cityMagnitude = 2.0f; // City limit - only brightest stars

        // Interpolate: as pollution increases, limit decreases toward 2.0
        float restrictedLimit = baseMagnitude * (1.0f - pollution) + cityMagnitude * pollution;

        return restrictedLimit;
    }

    /**
     * Get the reduction factor for aurora visibility
     */
    public static float getAuroraVisibilityFactor() {
        float pollution = getLightPollutionIntensity();

        // Auroras are large and bright, more resistant to light pollution
        // But still affected - moderate pollution can hide faint auroras
        return (float) Math.pow(1.0f - pollution, 1.2);
    }

    /**
     * Apply light pollution effect to sky colors (makes sky brighter/hazier)
     */
    public static float[] applyLightPollutionToSkyColor(float[] rgb, float altitude) {
        float pollution = getLightPollutionIntensity();

        if (pollution <= 0.0f) {
            return rgb;
        }

        // Light pollution is stronger near the horizon (low altitude)
        // altitude: 0.0 = horizon, 1.0 = zenith
        float altitudeFactor = 1.0f - Math.max(0.0f, Math.min(1.0f, altitude));
        float effectStrength = pollution * altitudeFactor * 0.5f; // Max 50% effect

        // Pollution adds orange/yellow glow (city lights)
        float pollutionR = 1.0f;
        float pollutionG = 0.8f;
        float pollutionB = 0.5f;

        // Blend original color with pollution color
        rgb[0] = rgb[0] * (1.0f - effectStrength) + pollutionR * effectStrength;
        rgb[1] = rgb[1] * (1.0f - effectStrength) + pollutionG * effectStrength;
        rgb[2] = rgb[2] * (1.0f - effectStrength) + pollutionB * effectStrength;

        // Also brighten the overall sky
        float brightnessBoost = effectStrength * 0.4f;
        rgb[0] = Math.min(1.0f, rgb[0] + brightnessBoost);
        rgb[1] = Math.min(1.0f, rgb[1] + brightnessBoost);
        rgb[2] = Math.min(1.0f, rgb[2] + brightnessBoost);

        return rgb;
    }

    /**
     * Get descriptive text for current light pollution level
     * Based on Bortle Dark Sky Scale
     */
    public static String getLightPollutionDescription() {
        float pollution = getLightPollutionIntensity();

        if (pollution < 0.1f) {
            return "Class 1: Excellent Dark Sky";
        } else if (pollution < 0.2f) {
            return "Class 2: Typical Dark Sky";
        } else if (pollution < 0.3f) {
            return "Class 3: Rural Sky";
        } else if (pollution < 0.4f) {
            return "Class 4: Rural/Suburban";
        } else if (pollution < 0.5f) {
            return "Class 5: Suburban Sky";
        } else if (pollution < 0.6f) {
            return "Class 6: Bright Suburban";
        } else if (pollution < 0.75f) {
            return "Class 7: Suburban/Urban";
        } else if (pollution < 0.9f) {
            return "Class 8: City Sky";
        } else {
            return "Class 9: Inner City";
        }
    }

    /**
     * Get expected visible star count based on pollution
     */
    public static int getVisibleStarCount() {
        float magnitude = getEffectiveMagnitudeLimit();

        // Approximate number of stars visible at different magnitude limits
        // This is based on real astronomical data
        if (magnitude >= 6.5f) return 9000;  // Dark sky
        if (magnitude >= 6.0f) return 5000;  // Rural
        if (magnitude >= 5.5f) return 2500;  // Rural/Suburban
        if (magnitude >= 5.0f) return 1000;  // Suburban
        if (magnitude >= 4.5f) return 500;   // Bright suburban
        if (magnitude >= 4.0f) return 250;   // Suburban/Urban
        if (magnitude >= 3.5f) return 100;   // Urban
        if (magnitude >= 3.0f) return 50;    // City
        if (magnitude >= 2.5f) return 25;    // Bright city
        return 10;                           // Inner city
    }
}