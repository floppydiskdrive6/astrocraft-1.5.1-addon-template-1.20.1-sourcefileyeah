package net.witherstorm8475.astrocraftaddon;

import net.fabricmc.api.ClientModInitializer;
import net.witherstorm8475.astrocraftaddon.position.PreccesingOrbit;

public class AstrocraftAddonClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("AstroCraft Addon: Initializing client...");

        // Apply precession to all planets
        try {
            PreccesingOrbit.applyPrecessionToPlanets();
            System.out.println("AstroCraft Addon: Precession system loaded successfully!");
        } catch (Exception e) {
            System.err.println("AstroCraft Addon: Failed to apply precession:");
            e.printStackTrace();
        }
    }
}