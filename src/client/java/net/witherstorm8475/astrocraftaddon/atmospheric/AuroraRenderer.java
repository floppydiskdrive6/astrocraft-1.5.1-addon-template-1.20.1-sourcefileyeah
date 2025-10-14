package net.witherstorm8475.astrocraftaddon.atmospheric;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AuroraRenderer {

    private static double animationTime = 0.0;
    private static AuroraCloudGenerator auroraGenerator = null;

    public static void render(Matrix4f viewMatrix, Matrix4f projMatrix, float tickDelta) {
        try {
            // Get current planet via reflection (already safe since Astrocraft is dev dependency)
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null) return;

            // Get planet name
            String bodyId = obsBody.getID();
            String[] parts = bodyId.split("\\.");
            String planetName = parts[parts.length - 1];

            // Get atmosphere config
            AtmosphericEvents.PlanetAtmosphere atmosphere = AtmosphericEvents.getAtmosphere(planetName);
            if (atmosphere == null || atmosphere.auroras == null || !atmosphere.auroras.enabled) return;

            // Update solar storm state
            double currentTime = PlanetManager.getPlanetTime();
            AtmosphericEvents.updateSolarStorms(planetName, currentTime);

            if (!AtmosphericEvents.isSolarStormActive(planetName)) return;

            // Get player latitude
            double latitude = SkyRenderer.getLatitude();

            boolean renderBorealis = latitude >= atmosphere.auroras.borealisMinLat &&
                    latitude <= atmosphere.auroras.borealisMaxLat;
            boolean renderAustralis = latitude >= atmosphere.auroras.australisMinLat &&
                    latitude <= atmosphere.auroras.australisMaxLat;

            if (!renderBorealis && !renderAustralis) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) animationTime += 0.016;

            double intensity = AtmosphericEvents.getSolarStormIntensity(planetName);
            if (auroraGenerator == null) auroraGenerator = new AuroraCloudGenerator(atmosphere.auroras);

            // ✅ Try reflection to access Better Clouds
            Object cloudsRenderer = null;
            boolean bcEnabled = false;
            try {
                Class<?> bcClass = Class.forName("com.qendolin.betterclouds.BetterClouds");
                Method getRenderer = bcClass.getDeclaredMethod("getCloudsRenderer");
                cloudsRenderer = getRenderer.invoke(null);

                Method isEnabled = bcClass.getDeclaredMethod("isEnabled");
                bcEnabled = (boolean) isEnabled.invoke(null);
            } catch (ClassNotFoundException ignored) {
                // Better Clouds not installed
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cloudsRenderer != null && bcEnabled) {
                renderUsingBetterClouds(cloudsRenderer, viewMatrix, projMatrix, tickDelta,
                        renderBorealis, renderAustralis, intensity, atmosphere.auroras);
            } else {
                renderFallback(viewMatrix, projMatrix, renderBorealis, renderAustralis,
                        intensity, atmosphere.auroras);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void renderUsingBetterClouds(Object cloudsRenderer, Matrix4f viewMatrix,
                                                Matrix4f projMatrix, float tickDelta,
                                                boolean renderBorealis, boolean renderAustralis,
                                                double intensity, AtmosphericEvents.AuroraConfig config) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            // Camera position (not used in reflection but may be handy later)
            Vector3d cameraPos = new Vector3d(
                    client.gameRenderer.getCamera().getPos().x,
                    client.gameRenderer.getCamera().getPos().y,
                    client.gameRenderer.getCamera().getPos().z
            );

            // Reflect Renderer.resources()
            Class<?> rendererClass = Class.forName("com.qendolin.betterclouds.clouds.Renderer");
            Method resourcesMethod = rendererClass.getDeclaredMethod("resources");
            resourcesMethod.setAccessible(true);
            Object resources = resourcesMethod.invoke(cloudsRenderer);

            // Reflect resources.generator()
            Class<?> resourcesClass = resources.getClass();
            Method generatorMethod = resourcesClass.getDeclaredMethod("generator");
            generatorMethod.setAccessible(true);
            Object generator = generatorMethod.invoke(resources);

            // Setup render state
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);

            if (renderBorealis) {
                renderAuroraBand(resources, viewMatrix, projMatrix, true, intensity, config);
            }
            if (renderAustralis) {
                renderAuroraBand(resources, viewMatrix, projMatrix, false, intensity, config);
            }

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();

        } catch (Exception e) {
            renderFallback(viewMatrix, projMatrix, renderBorealis, renderAustralis, intensity, config);
        }
    }

    private static void renderAuroraBand(Object resources, Matrix4f viewMatrix, Matrix4f projMatrix,
                                         boolean isBorealis, double intensity,
                                         AtmosphericEvents.AuroraConfig config) {
        try {
            Class<?> resourcesClass = resources.getClass();
            Method coverageShaderMethod = resourcesClass.getDeclaredMethod("coverageShader");
            coverageShaderMethod.setAccessible(true);
            Object coverageShader = coverageShaderMethod.invoke(resources);

            Method shadingShaderMethod = resourcesClass.getDeclaredMethod("shadingShader");
            shadingShaderMethod.setAccessible(true);
            Object shadingShader = shadingShaderMethod.invoke(resources);

            renderAuroraGeometry(coverageShader, shadingShader, viewMatrix, projMatrix,
                    isBorealis, intensity, config);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void renderAuroraGeometry(Object coverageShader, Object shadingShader,
                                             Matrix4f viewMatrix, Matrix4f projMatrix,
                                             boolean isBorealis, double intensity,
                                             AtmosphericEvents.AuroraConfig config) {
        // TODO: implement custom aurora rendering using Better Clouds shaders.
        // This is where you'd bind uniforms and draw your geometry.
    }

    private static void renderFallback(Matrix4f viewMatrix, Matrix4f projMatrix,
                                       boolean renderBorealis, boolean renderAustralis,
                                       double intensity, AtmosphericEvents.AuroraConfig config) {
        System.out.println("Using fallback aurora rendering (Better Clouds not available)");
    }

    public static void init() {
        AtmosphericEvents.load();
        System.out.println("Aurora renderer initialized - Better Clouds reflection integration enabled");
    }

    private static class AuroraCloudGenerator {
        private final AtmosphericEvents.AuroraConfig config;

        public AuroraCloudGenerator(AtmosphericEvents.AuroraConfig config) {
            this.config = config;
        }
    }
}
