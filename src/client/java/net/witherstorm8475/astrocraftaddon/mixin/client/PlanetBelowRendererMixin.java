package net.witherstorm8475.astrocraftaddon.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.ModelManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(WorldRenderer.class)
public class PlanetBelowRendererMixin {

    private Body cachedBody = null;
    private ModelManager.Model cachedModel = null;

    private void renderPlanetUnderPlayer(MatrixStack matrices, float tickDelta, long limitTime,
                                         Object camera, boolean renderBlockOutline,
                                         Object gameRenderer, CallbackInfo ci) {
        try {
            // Only reflect once to get the observed body
            if (cachedBody == null) {
                Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
                Field obsBodyField = planetManagerClass.getDeclaredField("obsBody");
                obsBodyField.setAccessible(true);
                cachedBody = (Body) obsBodyField.get(null);
            }

            if (cachedBody == null) return;

            // Only fetch the model once
            if (cachedModel == null) {
                cachedModel = ModelManager.getModel(cachedBody.getID());
            }

            if (cachedModel == null) return;

            // Player position
            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d playerPos = client.player.getPos();

            matrices.push();
            matrices.translate(playerPos.x, playerPos.y - 1.0, playerPos.z);
            matrices.scale(2.0f, 2.0f, 2.0f);

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // Bind the planet texture
            cachedModel.useTexture();

            // Get color from refColor
            float r = 0, g = 0, b = 0;
            try {
                Field refColorField = ModelManager.Model.class.getDeclaredField("refColor");
                refColorField.setAccessible(true);
                Vector3f ref = (Vector3f) refColorField.get(cachedModel);
                if (ref != null) {
                    r = ref.x;
                    g = ref.y;
                    b = ref.z;
                }
            } catch (Exception ignored) {}

            RenderSystem.setShaderColor(r, g, b, 1.0f);

            // Get texture
            Identifier tex = null;
            try {
                Field textureField = ModelManager.Model.class.getDeclaredField("texture");
                textureField.setAccessible(true);
                tex = (Identifier) textureField.get(cachedModel);
            } catch (Exception ignored) {}

            if (tex != null) {
                RenderLayer layer = RenderLayer.getEntitySolid(tex);
                VertexConsumerProvider.Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();
                VertexConsumer consumer = provider.getBuffer(layer);

                // Render a simple square under the player
                consumer.vertex(matrix, -16f, 0f, -16f).color(r, g, b, 1f).next();
                consumer.vertex(matrix, -16f, 0f, 16f).color(r, g, b, 1f).next();
                consumer.vertex(matrix, 16f, 0f, 16f).color(r, g, b, 1f).next();
                consumer.vertex(matrix, 16f, 0f, -16f).color(r, g, b, 1f).next();

                provider.draw();
            }

            matrices.pop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
