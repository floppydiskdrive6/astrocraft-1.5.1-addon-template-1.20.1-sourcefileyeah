package net.witherstorm8475.astrocraftaddon.mixin.client;

import mod.lwhrvw.astrocraft.planets.position.Orbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Orbit.class)
public interface OrbitAccessor {
    @Invoker("astrocraftAddon$setPrecession")
    void invokeSetPrecession(double nodalPeriod, double apsidalPeriod);
}
