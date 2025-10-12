package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.SkyRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.witherstorm8475.astrocraftaddon.atmospheric.ForestFireRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SkyRenderer.class, remap = false)
public class ForestFireRendererMixin {

    @Inject(method = "renderMain", at = @At("TAIL"), remap = false)
    private static void renderForestFires(CallbackInfo ci) {
        try {
            MatrixStack matrices = new MatrixStack();
            ForestFireRenderer.renderSkyTint(matrices);
        } catch (Exception e) {
            // Silently fail to avoid spam
        }
    }
}