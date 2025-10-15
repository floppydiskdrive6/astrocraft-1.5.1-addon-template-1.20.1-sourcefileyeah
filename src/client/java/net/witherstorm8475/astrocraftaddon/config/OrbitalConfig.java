package net.witherstorm8475.astrocraftaddon.config;

import java.util.ArrayList;
import java.util.List;

public class OrbitalConfig {

    // Always initialized lists to avoid null issues when reading JSON
    public List<PlanetOrbital> planets = new ArrayList<>();
    public List<MoonOrbital> moons = new ArrayList<>();

    // ===================== PLANETS =====================
    public static class PlanetOrbital {
        public String name = "Unknown";

        // Orbital mechanics
        public double nodal = 0.0;
        public double apsidal = 0.0;

        // Axial properties
        public double axialPrecessionPeriod = 0.0;
        public double minAxialTilt = 0.0;
        public double maxAxialTilt = 0.0;

        // Rotation
        public double Day = 1.0;
    }

    // ===================== MOONS =====================
    public static class MoonOrbital {
        public String name = "Unknown";

        // Axial properties
        public double axialPrecessionPeriod = 0.0;
        public double minAxialTilt = 0.0;
        public double maxAxialTilt = 0.0;

        // Rotation
        public double Day = 1.0;
    }
}
