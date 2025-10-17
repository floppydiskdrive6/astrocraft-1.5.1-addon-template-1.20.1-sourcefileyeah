package net.witherstorm8475.astrocraftaddon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.witherstorm8475.astrocraftaddon.config.AstrocraftAddonConfig;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;

public class AstrocraftAddonClient implements ClientModInitializer {

    private static boolean precessionApplied = false;
    private static int ticksWaited = 0;

    @Override
    public void onInitializeClient() {


        // Load the precession config first
        PreccesingOrbit.PrecessionConfig.load();


        AstrocraftAddonConfig.init();

        // Wait a few ticks for AstroCraft to initialize its planets
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            try {
                PreccesingOrbit.applyPrecessionToPlanets();
                precessionApplied = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}