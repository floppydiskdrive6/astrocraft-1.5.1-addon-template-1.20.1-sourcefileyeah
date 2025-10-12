package net.witherstorm8475.astrocraftaddon.atmospheric;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import mod.lwhrvw.astrocraft.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.lang.reflect.Field;

public class AuroraRenderer {

    private static final Identifier AURORA_TEXTURE = new Identifier("astrocraft-151-addon","textures/aurora.png");
    private static double animationTime = 0.0;

    public static void render() {
        try {
            // Get current planet
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null) {
                //System.out.println("DEBUG Aurora: obsBody is null");
                return;
            }

            // Get planet name
            String bodyId = obsBody.getID();
            String[] parts = bodyId.split("\\.");
            String planetName = parts[parts.length - 1];

            //System.out.println("DEBUG Aurora: On planet: " + planetName);

            // Get atmosphere config
            AtmosphericEvents.PlanetAtmosphere atmosphere = AtmosphericEvents.getAtmosphere(planetName);
            if (atmosphere == null) {
                //System.out.println("DEBUG Aurora: No atmosphere config for " + planetName);
                return;
            }
            if (atmosphere.auroras == null) {
                //System.out.println("DEBUG Aurora: No aurora config for " + planetName);
                return;
            }
            if (!atmosphere.auroras.enabled) {
                //System.out.println("DEBUG Aurora: Auroras disabled for " + planetName);
                return;
            }

            // Update solar storm state
            double currentTime = PlanetManager.getPlanetTime();
            AtmosphericEvents.updateSolarStorms(planetName, currentTime);

            // Only render during solar storms
            boolean stormActive = AtmosphericEvents.isSolarStormActive(planetName);
            //System.out.println("DEBUG Aurora: Storm active: " + stormActive + " at time: " + currentTime);

            if (!stormActive) {
                return;
            }

            // Get player latitude
            double latitude = SkyRenderer.getLatitude();
            //System.out.println("DEBUG Aurora: Player latitude: " + latitude);

            // Determine which aurora to render
            boolean renderBorealis = latitude >= atmosphere.auroras.borealisMinLat &&
                    latitude <= atmosphere.auroras.borealisMaxLat;
            boolean renderAustralis = latitude >= atmosphere.auroras.australisMinLat &&
                    latitude <= atmosphere.auroras.australisMaxLat;

            //System.out.println("DEBUG Aurora: Borealis: " + renderBorealis + ", Australis: " + renderAustralis);

            if (!renderBorealis && !renderAustralis) {
                return; // Not in aurora zone
            }

            // Update animation time
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
                animationTime += 0.016; // Approximately 1/60th of a second
            }

            // Get storm intensity for alpha
            double intensity = AtmosphericEvents.getSolarStormIntensity(planetName);
            //System.out.println("DEBUG Aurora: RENDERING AURORA! Intensity: " + intensity);

            // Render auroras
            if (renderBorealis) {
                renderAuroraRibbon(atmosphere.auroras, true, intensity);
            }
            if (renderAustralis) {
                renderAuroraRibbon(atmosphere.auroras, false, intensity);
            }

        } catch (Exception e) {
            System.err.println("DEBUG Aurora: Exception!");
            e.printStackTrace();
        }
    }

    private static void renderAuroraRibbon(AtmosphericEvents.AuroraConfig config,
                                           boolean isBorealis, double intensity) {
        // Setup render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // Bind aurora texture
        RenderSystem.setShaderTexture(0, AURORA_TEXTURE);
        RenderSystem.setShader(RenderUtils.getPositionTexProgram());

        // Render multiple layers with different colors
        for (int i = 0; i < config.colors.length; i++) {
            int color = config.colors[i];
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = (float) (intensity * 0.6f); // Base alpha from intensity

            RenderSystem.setShaderColor(r, g, b, a);

            // Calculate offset for this layer
            double layerOffset = i * 20.0; // Vertical separation between layers

            // Render the ribbon with wave animation
            renderAuroraSegments(config, isBorealis, layerOffset);
        }

        // Restore render state
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderAuroraSegments(AtmosphericEvents.AuroraConfig config,
                                             boolean isBorealis, double layerOffset) {
        int segments = 20; // Number of ribbon segments
        double width = 5000.0; // Total width
        double segmentWidth = width / segments;

        // Calculate base height
        double baseHeight = config.height + layerOffset;

        for (int i = 0; i < segments; i++) {
            double x1 = -width / 2.0 + i * segmentWidth;
            double x2 = x1 + segmentWidth;

            // Calculate wave offset for vertical animation
            double phase1 = (x1 / 500.0) + (animationTime * config.waveSpeed);
            double phase2 = (x2 / 500.0) + (animationTime * config.waveSpeed);

            double wave1 = Math.sin(phase1) * config.waveAmplitude;
            double wave2 = Math.sin(phase2) * config.waveAmplitude;

            // Calculate horizontal flow
            double flow = animationTime * config.horizontalFlowSpeed * 100.0;
            x1 += flow;
            x2 += flow;

            // Wrap around
            if (x1 > width / 2.0) x1 -= width;
            if (x2 > width / 2.0) x2 -= width;

            // Calculate Z position based on hemisphere
            double z = isBorealis ? -2000.0 : 2000.0;

            // Bottom edge of ribbon
            double y1 = baseHeight + wave1;
            double y2 = baseHeight + wave2;

            // Top edge of ribbon
            double y1Top = y1 + config.thickness;
            double y2Top = y2 + config.thickness;

            // Render quad
            Matrix4f matrix = new Matrix4f();
            matrix.identity();

            try {
                // Use RenderUtils.BufferHelper to draw a textured quad
                // We'll render a simple plane and position it
                matrix.translate((float) ((x1 + x2) / 2.0),
                        (float) ((y1 + y2) / 2.0),
                        (float) z);
                matrix.scale((float) segmentWidth, (float) config.thickness, 1.0f);

                new RenderUtils.BufferHelper(1.0f).texture().draw(matrix);
            } catch (Exception e) {
                // Continue rendering other segments
            }
        }
    }

    public static void init() {
        // Initialize aurora renderer
        AtmosphericEvents.load();
        System.out.println("Aurora renderer initialized");
    }
}