package net.witherstorm8475.astrocraftaddon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.witherstorm8475.astrocraftaddon.atmospheric.AuroraRenderer;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;

public class AstrocraftAddonClient implements ClientModInitializer {

    private static boolean precessionApplied = false;
    private static int ticksWaited = 0;

    @Override
    public void onInitializeClient() {
        System.out.println("AstroCraft Addon: Initializing client...");

        // Load the precession config first
        PreccesingOrbit.PrecessionConfig.load();

        // Initialize aurora renderer
        //AuroraRenderer.init();

        // Wait a few ticks for AstroCraft to initialize its planets
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            try {
                //System.out.println("Attempting to apply precession...");
                PreccesingOrbit.applyPrecessionToPlanets();
                precessionApplied = true;
                //System.out.println("AstroCraft Addon: Precession system loaded successfully!");
            } catch (Exception e) {
                System.err.println("AstroCraft Addon: Failed to apply precession:");
                e.printStackTrace();
            }
        });
    }
}