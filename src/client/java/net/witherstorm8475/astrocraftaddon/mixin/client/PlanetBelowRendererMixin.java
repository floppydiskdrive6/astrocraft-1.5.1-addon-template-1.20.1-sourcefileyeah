package net.witherstorm8475.astrocraftaddon.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.ModelManager;
import mod.lwhrvw.astrocraft.planets.Planet;
import mod.lwhrvw.astrocraft.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(value = SkyRenderer.class, remap = false)
public class PlanetBelowRendererMixin {

    @Inject(method = "renderMain", at = @At("TAIL"), remap = false)
    private static void renderPlanetBelow(CallbackInfo ci) {
        try {
            // Get the observer body (current planet player is on)
            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
            obsBodyField.setAccessible(true);
            Body obsBody = (Body) obsBodyField.get(null);

            if (obsBody == null || !(obsBody instanceof Planet)) {
                return; // Not on a planet
            }

            Planet currentPlanet = (Planet) obsBody;

            // Get the planet's model ID using reflection
            Field modelIDField = Planet.class.getDeclaredField("modelID");
            modelIDField.setAccessible(true);
            String modelID = (String) modelIDField.get(currentPlanet);

            if (modelID == null) {
                return;
            }

            // Get the model
            ModelManager.Model model = ModelManager.getModel(modelID);
            if (model == null) {
                return;
            }

            // Get Minecraft client and ensure player is present
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                return;
            }

            // Render the planet texture as a large plane below
            renderPlanetTexturePlane(model);

        } catch (Exception e) {
            // Silently fail
        }
    }

    private static void renderPlanetTexturePlane(ModelManager.Model model) {
        // Enable blending and depth test
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false); // Don't write to depth buffer

        // Bind the planet's texture
        model.useTexture();

        // Set shader
        RenderSystem.setShader(RenderUtils.getPositionTexProgram());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Create identity matrix (render in world space below everything)
        Matrix4f matrix = new Matrix4f();
        matrix.identity();

        // Translate down and scale up
        matrix.translate(0.0F, -5000.0F, 0.0F); // 5000 blocks below
        matrix.scale(50000.0F, 1.0F, 50000.0F); // 100km x 100km plane

        // Render using RenderUtils.BufferHelper
        // This creates a textured quad
        new RenderUtils.BufferHelper(1.0F).texture().draw(matrix);

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
