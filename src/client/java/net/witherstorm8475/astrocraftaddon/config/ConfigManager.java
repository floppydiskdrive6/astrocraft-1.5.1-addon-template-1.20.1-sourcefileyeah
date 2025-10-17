package net.witherstorm8475.astrocraftaddon.config;

import jdk.jfr.Name;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;

import java.util.ArrayList;
import java.util.List;

@Config(name = "astrocraft-addon")
public class ConfigManager implements ConfigData {

    @Category("Visual")
    @CollapsibleObject
    public VisualSettings visual = new VisualSettings();

    @Category("Orbital")
    @CollapsibleObject
    public OrbitalSettings orbital = new OrbitalSettings();

    @Override
    public void validatePostLoad() {
        if (visual.planets.isEmpty() && visual.moons.isEmpty()) {
            visual.initializeDefaults();
        }
        if (orbital.planets.isEmpty() && orbital.moons.isEmpty()) {
            orbital.initializeDefaults();
        }
    }

    // ==========================
    //      VISUAL SETTINGS
    // ==========================
    public static class VisualSettings {

        public List<PlanetVisualConfig> planets = new ArrayList<>();
        public List<MoonVisualConfig> moons = new ArrayList<>();

        public VisualSettings() {
            if (planets.isEmpty() && moons.isEmpty()) {
                initializeDefaults();
            }
        }

        public void initializeDefaults() {
            planets.add(new PlanetVisualConfig("Venus", false, 0, 0,
                    "#FFFFFF", "#FFFFFF", 0, 0, 0, 0,
                    "#FFDAB9", "#FFCC99", "#FFE4B5", "#2F1E0F", "#FFC87C"));

            planets.add(new PlanetVisualConfig("Earth", true, 0.7, 5000.0,
                    "#FFAA00", "#FF4400", 2.0, 10.0, 50.0, 100.0,
                    "#FFA040", "#FF6020", "#87CEEB", "#000814", "#FFB080"));

            planets.add(new PlanetVisualConfig("Mars", false, 0, 0,
                    "#FFFFFF", "#FFFFFF", 0, 0, 0, 0,
                    "#4080C0", "#2060A0", "#E8C9A6", "#1A1410", "#C09060"));

            moons.add(new MoonVisualConfig("Titan", false, 0, 0,
                    "#FFFFFF", "#FFFFFF", 0, 0, 0, 0,
                    "#FFDDAA", "#FFCC88", "#FFE8AA", "#1A0F0F", "#FFCC88"));
        }
    }

    // ==========================
    //     VISUAL STRUCTURES
    // ==========================
    public static class PlanetVisualConfig {
        public String name;
        public boolean forestFiresEnabled;
        public double maxTintStrength;
        public double spreadRadius;
        public String skyTintStart;
        public String skyTintEnd;
        public double minDuration;
        public double maxDuration;
        public double minInterval;
        public double maxInterval;
        public String sunriseColor;
        public String sunsetColor;
        public String dayColor;
        public String nightColor;
        public String horizonColor;

        public PlanetVisualConfig() {}

        public PlanetVisualConfig(String name, boolean forestFiresEnabled, double maxTintStrength, double spreadRadius,
                                  String skyTintStart, String skyTintEnd, double minDuration, double maxDuration,
                                  double minInterval, double maxInterval, String sunriseColor, String sunsetColor,
                                  String dayColor, String nightColor, String horizonColor) {
            this.name = name;
            this.forestFiresEnabled = forestFiresEnabled;
            this.maxTintStrength = maxTintStrength;
            this.spreadRadius = spreadRadius;
            this.skyTintStart = skyTintStart;
            this.skyTintEnd = skyTintEnd;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.minInterval = minInterval;
            this.maxInterval = maxInterval;
            this.sunriseColor = sunriseColor;
            this.sunsetColor = sunsetColor;
            this.dayColor = dayColor;
            this.nightColor = nightColor;
            this.horizonColor = horizonColor;
        }
    }

    public static class MoonVisualConfig {
        public String name;
        public boolean forestFiresEnabled;
        public double maxTintStrength;
        public double spreadRadius;
        public String skyTintStart;
        public String skyTintEnd;
        public double minDuration;
        public double maxDuration;
        public double minInterval;
        public double maxInterval;
        public String sunriseColor;
        public String sunsetColor;
        public String dayColor;
        public String nightColor;
        public String horizonColor;

        public MoonVisualConfig() {}

        public MoonVisualConfig(String name, boolean forestFiresEnabled, double maxTintStrength, double spreadRadius,
                                String skyTintStart, String skyTintEnd, double minDuration, double maxDuration,
                                double minInterval, double maxInterval, String sunriseColor, String sunsetColor,
                                String dayColor, String nightColor, String horizonColor) {
            this.name = name;
            this.forestFiresEnabled = forestFiresEnabled;
            this.maxTintStrength = maxTintStrength;
            this.spreadRadius = spreadRadius;
            this.skyTintStart = skyTintStart;
            this.skyTintEnd = skyTintEnd;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.minInterval = minInterval;
            this.maxInterval = maxInterval;
            this.sunriseColor = sunriseColor;
            this.sunsetColor = sunsetColor;
            this.dayColor = dayColor;
            this.nightColor = nightColor;
            this.horizonColor = horizonColor;
        }
    }

    // ==========================
    //    ORBITAL SETTINGS
    // ==========================
    public static class OrbitalSettings {

        public List<PlanetOrbitalConfig> planets = new ArrayList<>();
        public List<MoonOrbitalConfig> moons = new ArrayList<>();

        public OrbitalSettings() {
            if (planets.isEmpty() && moons.isEmpty()) {
                initializeDefaults();
            }
        }

        private void initializeDefaults() {
            planets.add(new PlanetOrbitalConfig("Mercury", 0.01, 0.034, 325513.0, 176.0, 325513.0, 280000.0));
            planets.add(new PlanetOrbitalConfig("Venus", 2.64, 2.64, 0.0, -116.75, 29000.0, 29000.0));
            planets.add(new PlanetOrbitalConfig("Earth", 22.1, 24.5, 25772.0, 1.0, 25772.0, 112000.0));
            planets.add(new PlanetOrbitalConfig("Mars", 22.04, 26.14, 170000.0, 1.027, 170000.0, 7300.0));
            planets.add(new PlanetOrbitalConfig("Jupiter", 3.13, 3.13, 0.0, 0.41354, 50687.0, 200000.0));
            planets.add(new PlanetOrbitalConfig("Saturn", 26.73, 26.73, 0.0, 0.44401, 50687.0, 1400000.0));
            planets.add(new PlanetOrbitalConfig("Uranus", 82.23, 82.23, 0.0, -0.71833, 0.0, 0.0));
            planets.add(new PlanetOrbitalConfig("Neptune", 28.32, 28.32, 0.0, 0.67125, 0.0, 0.0));
            planets.add(new PlanetOrbitalConfig("Pluto", 102.0, 126.0, 3000000.0, 6.4, 20000.0, 19951.0));

            moons.add(new MoonOrbitalConfig("Moon", 1.54, 1.54, 18.6, 29.530589057));
            moons.add(new MoonOrbitalConfig("Phobos", 1.1, 1.1, 2.262, 0.31891));
            moons.add(new MoonOrbitalConfig("Deimos", 1.8, 1.8, 54.537, 1.262361));
            moons.add(new MoonOrbitalConfig("Io", 0.0, 0.0, 1.333, 1.769861));
            moons.add(new MoonOrbitalConfig("Europa", 0.5, 0.5, 30.202, 3.554095));
            moons.add(new MoonOrbitalConfig("Ganymede", 0.2, 0.2, 137.812, 7.166389));
            moons.add(new MoonOrbitalConfig("Callisto", 0.3, 0.3, 577.264, 16.753563));
            moons.add(new MoonOrbitalConfig("Titan", 0.33, 0.33, 0.0, 15.95));
        }
    }

    // ==========================
    //    ORBITAL STRUCTURES
    // ==========================
    public static class PlanetOrbitalConfig {
        public String name;
        public double minAxialTilt;
        public double maxAxialTilt;
        public double axialPrecessionPeriod;
        public double dayLength;
        public double nodalPrecession;
        public double apsidalPrecession;

        public PlanetOrbitalConfig() {}

        public PlanetOrbitalConfig(String name, double minAxialTilt, double maxAxialTilt, double axialPrecessionPeriod,
                                   double dayLength, double nodalPrecession, double apsidalPrecession) {
            this.name = name;
            this.minAxialTilt = minAxialTilt;
            this.maxAxialTilt = maxAxialTilt;
            this.axialPrecessionPeriod = axialPrecessionPeriod;
            this.dayLength = dayLength;
            this.nodalPrecession = nodalPrecession;
            this.apsidalPrecession = apsidalPrecession;
        }
    }

    public static class MoonOrbitalConfig {
        public String name;
        public double minAxialTilt;
        public double maxAxialTilt;
        public double axialPrecessionPeriod;
        public double dayLength;

        public MoonOrbitalConfig() {}

        public MoonOrbitalConfig(String name, double minAxialTilt, double maxAxialTilt,
                                 double axialPrecessionPeriod, double dayLength) {
            this.name = name;
            this.minAxialTilt = minAxialTilt;
            this.maxAxialTilt = maxAxialTilt;
            this.axialPrecessionPeriod = axialPrecessionPeriod;
            this.dayLength = dayLength;
        }
    }
}
