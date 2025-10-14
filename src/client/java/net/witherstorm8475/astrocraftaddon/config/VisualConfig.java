package net.witherstorm8475.astrocraftaddon.config;

import java.util.ArrayList;
import java.util.List;

public class VisualConfig {
    public List<PlanetVisual> planets = new ArrayList<>();
    public List<MoonVisual> moons = new ArrayList<>();

    public static class PlanetVisual {
        public String name = "Unknown";

        public ForestFire forestFires = new ForestFire();
        public SkyColors skyColors = new SkyColors();

        public static class ForestFire {
            public boolean enabled = false;
            public double minDuration = 0, maxDuration = 0, minInterval = 0, maxInterval = 0;
            public double maxTintStrength = 0, spreadRadius = 0;
            public String skyTintStart = "#000000", skyTintEnd = "#000000";
        }

        public static class SkyColors {
            public String sunriseColor = "#FFFFFF";
            public String sunsetColor = "#FFFFFF";
            public String dayColor = "#FFFFFF";
            public String nightColor = "#000000";
            public String horizonColor = "#FFFFFF";
        }
    }

    public static class MoonVisual {
        public String name = "Unknown";
        public PlanetVisual.ForestFire forestFires = new PlanetVisual.ForestFire();
        public PlanetVisual.SkyColors skyColors = new PlanetVisual.SkyColors();
    }
}