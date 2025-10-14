package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.SkyRenderer;
import net.witherstorm8475.astrocraftaddon.atmospheric.AuroraRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(value = SkyRenderer.class, remap = false)
public class AuroraRendererMixin {

    @Inject(method = "renderMain", at = @At("TAIL"), remap = false)
    private static void renderAuroras(CallbackInfo ci) {
        try {
            // reflect AuroraRenderer.render(Matrix4f, Matrix4f, float)
            Class<?> auroraClass = Class.forName("net.witherstorm8475.astrocraftaddon.atmospheric.AuroraRenderer");
            Method renderMethod = auroraClass.getDeclaredMethod("render",
                    Class.forName("org.joml.Matrix4f"),
                    Class.forName("org.joml.Matrix4f"),
                    float.class
            );
            renderMethod.setAccessible(true);

            // If you don't have these matrices, pass null and tickDelta=0
            renderMethod.invoke(null, null, null, 0.0f);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
