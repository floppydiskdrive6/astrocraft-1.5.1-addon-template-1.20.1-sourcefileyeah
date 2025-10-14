package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.SkyRenderer;
import net.witherstorm8475.astrocraftaddon.atmospheric.AuroraRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SkyRenderer.class, remap = false)
public class AuroraRendererMixin {

    @Inject(method = "renderMain", at = @At("TAIL"), remap = false)
    private static void renderAuroras(Matrix4f viewMatrix, Matrix4f projMatrix, float tickDelta, CallbackInfo ci) {
        try {
            AuroraRenderer.render(viewMatrix, projMatrix, tickDelta);
        } catch (Exception e) {
            // Avoid crashing or spamming logs if rendering fails
        }
    }
}
