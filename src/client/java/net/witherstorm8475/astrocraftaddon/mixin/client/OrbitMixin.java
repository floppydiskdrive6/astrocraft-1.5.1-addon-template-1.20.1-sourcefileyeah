package net.witherstorm8475.astrocraftaddon.mixin;

import mod.lwhrvw.astrocraft.planets.position.Orbit;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Orbit.class, remap = false)
public class OrbitMixin {

    @Shadow
    protected double longAscendingAE;

    @Shadow
    protected double longPeriapsisAE;

    @Shadow
    protected double longMeanAE;

    @Shadow
    protected double period;

    @Shadow
    protected double getTimeSinceEpoch(double time) {
        return 0;
    }

    @Unique
    private double astrocraftAddon$nodalPrecPeriod = 0.0;

    @Unique
    private boolean astrocraftAddon$precessionApplied = false;

    @Unique
    private double astrocraftAddon$apsidalPrecPeriod = 0.0;

    @Inject(method = "getLongAscending", at = @At("HEAD"), cancellable = true, remap = false)
    private void modifyLongAscending(double time, CallbackInfoReturnable<Double> cir) {
        if (astrocraftAddon$precessionApplied && astrocraftAddon$nodalPrecPeriod != 0.0) {
            System.out.println("NODAL PRECESSION: time=" + time + ", period=" + astrocraftAddon$nodalPrecPeriod);
            double result = longAscendingAE - 360.0 * time / astrocraftAddon$nodalPrecPeriod;
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "getArgPeriapsis", at = @At("HEAD"), cancellable = true, remap = false)
    private void modifyArgPeriapsis(double time, CallbackInfoReturnable<Double> cir) {
        if (astrocraftAddon$precessionApplied) {
            System.out.println("APSIDAL PRECESSION CALLED: time=" + time + ", period=" + astrocraftAddon$apsidalPrecPeriod);
            double longAsc = astrocraftAddon$nodalPrecPeriod == 0.0
                    ? longAscendingAE
                    : longAscendingAE - 360.0 * time / astrocraftAddon$nodalPrecPeriod;

            if (astrocraftAddon$apsidalPrecPeriod == 0.0) {
                cir.setReturnValue(longPeriapsisAE - longAsc);
            } else {
                double result = longPeriapsisAE - longAsc + 360.0 * time / astrocraftAddon$apsidalPrecPeriod;
                System.out.println("APSIDAL RESULT: " + result);
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(method = "getMeanAnomaly", at = @At("HEAD"), cancellable = true, remap = false)
    private void modifyMeanAnomaly(double time, CallbackInfoReturnable<Double> cir) {
        if (astrocraftAddon$precessionApplied) {
            double longAsc = astrocraftAddon$nodalPrecPeriod == 0.0
                    ? longAscendingAE
                    : longAscendingAE - 360.0 * time / astrocraftAddon$nodalPrecPeriod;

            double argPeri;
            if (astrocraftAddon$apsidalPrecPeriod == 0.0) {
                argPeri = longPeriapsisAE - longAsc;
            } else {
                argPeri = longPeriapsisAE - longAsc + 360.0 * time / astrocraftAddon$apsidalPrecPeriod;
            }

            double result = longMeanAE - longAsc - argPeri + 360.0 * getTimeSinceEpoch(time) / period;
            cir.setReturnValue(result);
        }
    }

    @Unique
    public void astrocraftAddon$setPrecession(double nodalPeriod, double apsidalPeriod) {
        this.astrocraftAddon$nodalPrecPeriod = nodalPeriod;
        this.astrocraftAddon$apsidalPrecPeriod = apsidalPeriod;
        this.astrocraftAddon$precessionApplied = true;
    }
}