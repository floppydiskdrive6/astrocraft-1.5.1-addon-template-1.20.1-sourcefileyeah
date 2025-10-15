package net.witherstorm8475.astrocraftaddon.config;

import java.util.ArrayList;
import java.util.List;

public class VisualConfig {

    // Light pollution support (matches your JSON structure)
    public LightPollution lightPollution = new LightPollution();

    // Always initialized lists to avoid null issues
    public List<PlanetVisual> planets = new ArrayList<>();
    public List<MoonVisual> moons = new ArrayList<>();

    // ===================== LIGHT POLLUTION =====================
    public static class LightPollution {
        public String mode = "dynamic"; // or "static"
        public double intensity = 1.0;
    }

    // ===================== PLANETS =====================
    public static class PlanetVisual {
        public String name = "Unknown";
        public ForestFire forestFires = new ForestFire();
        public SkyColors skyColors = new SkyColors();

        public static class ForestFire {
            public boolean enabled = false;
            public double minDuration = 0;
            public double maxDuration = 0;
            public double minInterval = 0;
            public double maxInterval = 0;
            public double maxTintStrength = 0;
            public double spreadRadius = 0;
            public String skyTintStart = "#000000";
            public String skyTintEnd = "#000000";
        }

        public static class SkyColors {
            public String sunriseColor = "#FFFFFF";
            public String sunsetColor = "#FFFFFF";
            public String dayColor = "#FFFFFF";
            public String nightColor = "#000000";
            public String horizonColor = "#FFFFFF";
        }
    }

    // ===================== MOONS =====================
    public static class MoonVisual {
        public String name = "Unknown";
        public PlanetVisual.ForestFire forestFires = new PlanetVisual.ForestFire();
        public PlanetVisual.SkyColors skyColors = new PlanetVisual.SkyColors();
    }
}
