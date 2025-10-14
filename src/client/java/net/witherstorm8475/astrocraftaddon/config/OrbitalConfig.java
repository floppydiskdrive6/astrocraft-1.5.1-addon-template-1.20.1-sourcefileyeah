package net.witherstorm8475.astrocraftaddon.config;

import java.util.ArrayList;
import java.util.List;

public class OrbitalConfig {
    public List<PlanetOrbital> planets = new ArrayList<>();
    public List<MoonOrbital> moons = new ArrayList<>();

    public static class PlanetOrbital {
        public String name = "Unknown";
        public double nodal = 0;
        public double apsidal = 0;
        public double axialPrecessionPeriod = 0;
        public double minAxialTilt = 0;
        public double maxAxialTilt = 0;
        public double Day = 1;
    }

    public static class MoonOrbital {
        public String name = "Unknown";
        public double axialPrecessionPeriod = 0;
        public double minAxialTilt = 0;
        public double maxAxialTilt = 0;
        public double Day = 1;
    }
}