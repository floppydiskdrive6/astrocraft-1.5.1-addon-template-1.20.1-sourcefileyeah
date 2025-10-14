package net.witherstorm8475.astrocraftaddon.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.lwhrvw.astrocraft.SkyRenderer;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.ModelManager;
import mod.lwhrvw.astrocraft.planets.Planet;
import mod.lwhrvw.astrocraft.utils.RenderUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = SkyRenderer.class, remap = false)
public class PlanetBelowRendererMixin {

    @Inject(method = "renderMain", at = @At("RETURN"), remap = false)
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

            // Render the planet texture as a large plane below
            renderPlanetTexturePlane(model);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void renderPlanetTexturePlane(ModelManager.Model model) {
        try {
            RenderSystem.disableCull();

            // Bind texture
            model.useTexture();

            // Set shader and color
            RenderSystem.setShader(RenderUtils.getPositionTexProgram());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Create matrix - VERY close and HUGE for testing
            Matrix4f matrix = new Matrix4f();
            matrix.identity();
            matrix.translate(0.0F, -100.0F, 0.0F); // Only 100 blocks down
            matrix.rotateX((float) Math.PI);
            matrix.scale(50000.0F, 1.0F, 50000.0F); // 100,000 blocks wide!

            // Use BufferHelper
            RenderUtils.BufferHelper helper = new RenderUtils.BufferHelper(1.0F);
            helper.texture();
            helper.draw(matrix);

            RenderSystem.enableCull();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}