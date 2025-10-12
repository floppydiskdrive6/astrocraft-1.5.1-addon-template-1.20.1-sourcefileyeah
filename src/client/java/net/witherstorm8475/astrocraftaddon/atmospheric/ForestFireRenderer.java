package net.witherstorm8475.astrocraftaddon.atmospheric;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.lang.reflect.Field;

public class ForestFireRenderer {

    public static void renderSkyTint(MatrixStack matrices) {
        try {
            // Get current planet
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
            if (atmosphere == null || atmosphere.forestFires == null || !atmosphere.forestFires.enabled) {
                return;
            }

            // Get player position
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            double playerX = client.player.getX();
            double playerZ = client.player.getZ();

            // Update forest fire state
            double currentTime = PlanetManager.getPlanetTime();
            AtmosphericEvents.updateForestFires(planetName, currentTime, playerX, playerZ);

            // Only render during forest fires
            if (!AtmosphericEvents.isForestFireActive(planetName)) {
                return;
            }

            // Get fire state
            AtmosphericEvents.ForestFireState fireState = AtmosphericEvents.getForestFireState(planetName);
            if (fireState == null) return;

            // Calculate distance from fire center
            double dx = playerX - fireState.centerX;
            double dz = playerZ - fireState.centerZ;
            double distance = Math.sqrt(dx * dx + dz * dz);

            // Calculate tint strength based on distance
            double distanceFactor = 1.0 - Math.min(1.0, distance / atmosphere.forestFires.spreadRadius);
            double tintStrength = distanceFactor * atmosphere.forestFires.maxTintStrength * fireState.intensity;

            if (tintStrength < 0.01) return; // Too far away

            // Interpolate between start and end colors based on intensity
            int startColor = atmosphere.forestFires.skyTintStart;
            int endColor = atmosphere.forestFires.skyTintEnd;

            float startR = ((startColor >> 16) & 0xFF) / 255.0f;
            float startG = ((startColor >> 8) & 0xFF) / 255.0f;
            float startB = (startColor & 0xFF) / 255.0f;

            float endR = ((endColor >> 16) & 0xFF) / 255.0f;
            float endG = ((endColor >> 8) & 0xFF) / 255.0f;
            float endB = (endColor & 0xFF) / 255.0f;

            float t = (float) fireState.intensity;
            float r = startR + (endR - startR) * t;
            float g = startG + (endG - startG) * t;
            float b = startB + (endB - startB) * t;
            float a = (float) tintStrength;

            // Render full-screen tint overlay
            renderFullScreenTint(matrices, r, g, b, a);

            //System.out.println("DEBUG Fire: Rendering sky tint - strength: " + tintStrength + " distance: " + distance);

        } catch (Exception e) {
            System.err.println("DEBUG Fire: Exception!");
            e.printStackTrace();
        }
    }

    private static void renderFullScreenTint(MatrixStack matrices, float r, float g, float b, float a) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        // ✅ Manual ModelViewProjection matrix for 1.20.1
        Matrix4f matrix = new Matrix4f(RenderSystem.getProjectionMatrix());
        matrix.mul(RenderSystem.getModelViewMatrix());

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Fullscreen quad in NDC space
        bufferBuilder.vertex(matrix, -1.0f, -1.0f, 0.0f).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix,  1.0f, -1.0f, 0.0f).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix,  1.0f,  1.0f, 0.0f).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, -1.0f,  1.0f, 0.0f).color(r, g, b, a).next();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }
}
