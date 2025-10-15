package net.witherstorm8475.astrocraftaddon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static VisualConfig visualConfig = new VisualConfig();
    public static OrbitalConfig orbitalConfig = new OrbitalConfig();

    private static final String CONFIG_FOLDER = "config/astrocraft-151-addon";
    private static final String VISUAL_FILE_NAME = "atmosphericevents.json";
    private static final String ORBITAL_FILE_NAME = "precession.json";

    public static void load() {
        System.out.println("[AstroCraft Addon] Loading configs...");

        File folder = new File(CONFIG_FOLDER);
        if (!folder.exists()) {
            System.out.println("[AstroCraft Addon] Config folder doesn't exist, creating...");
            folder.mkdirs();
        }

        File visualFile = new File(folder, VISUAL_FILE_NAME);
        File orbitalFile = new File(folder, ORBITAL_FILE_NAME);

        System.out.println("[AstroCraft Addon] Visual file exists: " + visualFile.exists());
        System.out.println("[AstroCraft Addon] Orbital file exists: " + orbitalFile.exists());

        boolean visualExists = visualFile.exists();
        boolean orbitalExists = orbitalFile.exists();

        // --- Load Visual Config ---
        if (visualExists) {
            try (Reader reader = new FileReader(visualFile)) {
                VisualConfig loaded = GSON.fromJson(reader, VisualConfig.class);
                if (loaded != null) {
                    visualConfig = loaded;
                } else {
                    System.out.println("[Astrocraft Addon] Visual config loaded as null, using defaults.");
                    visualConfig = createDefaultVisualConfig();
                }
            } catch (IOException e) {
                System.err.println("[Astrocraft Addon] Error loading visual config, using defaults.");
                e.printStackTrace();
                visualConfig = createDefaultVisualConfig();
            }
        } else {
            System.out.println("[Astrocraft Addon] Visual config not found, creating default.");
            visualConfig = createDefaultVisualConfig();
        }

        // --- Load Orbital Config ---
        if (orbitalExists) {
            try (Reader reader = new FileReader(orbitalFile)) {
                OrbitalConfig loaded = GSON.fromJson(reader, OrbitalConfig.class);
                if (loaded != null) {
                    orbitalConfig = loaded;
                } else {
                    System.out.println("[Astrocraft Addon] Orbital config loaded as null, using defaults.");
                    orbitalConfig = createDefaultOrbitalConfig();
                }
            } catch (IOException e) {
                System.err.println("[Astrocraft Addon] Error loading orbital config, using defaults.");
                e.printStackTrace();
                orbitalConfig = createDefaultOrbitalConfig();
            }
        } else {
            System.out.println("[Astrocraft Addon] Orbital config not found, creating default.");
            orbitalConfig = createDefaultOrbitalConfig();
        }

        // Ensure neither is null and lists are initialized
        if (visualConfig == null) {
            visualConfig = createDefaultVisualConfig();
        }
        if (orbitalConfig == null) {
            orbitalConfig = createDefaultOrbitalConfig();
        }

        // Ensure lists are not null
        if (visualConfig.planets == null) visualConfig.planets = new java.util.ArrayList<>();
        if (visualConfig.moons == null) visualConfig.moons = new java.util.ArrayList<>();
        if (orbitalConfig.planets == null) orbitalConfig.planets = new java.util.ArrayList<>();
        if (orbitalConfig.moons == null) orbitalConfig.moons = new java.util.ArrayList<>();

        // Save the defaults if files didn't exist
        if (!visualExists || !orbitalExists) {
            save();
        }
    }

    public static void save() {
        File folder = new File(CONFIG_FOLDER);
        if (!folder.exists()) folder.mkdirs();

        File visualFile = new File(folder, VISUAL_FILE_NAME);
        File orbitalFile = new File(folder, ORBITAL_FILE_NAME);

        try (Writer writer = new FileWriter(visualFile)) {
            GSON.toJson(visualConfig, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Writer writer = new FileWriter(orbitalFile)) {
            GSON.toJson(orbitalConfig, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        load();
    }

    // ========== DEFAULT CONFIGS ==========

    private static VisualConfig createDefaultVisualConfig() {
        VisualConfig config = new VisualConfig();

        // Venus
        VisualConfig.PlanetVisual venus = new VisualConfig.PlanetVisual();
        venus.name = "Venus";
        venus.forestFires.enabled = false;
        venus.forestFires.maxTintStrength = 0;
        venus.forestFires.spreadRadius = 0;
        venus.forestFires.skyTintStart = "#FFFFFF";
        venus.forestFires.skyTintEnd = "#FFFFFF";
        venus.forestFires.minDuration = 0;
        venus.forestFires.maxDuration = 0;
        venus.forestFires.minInterval = 0;
        venus.forestFires.maxInterval = 0;
        venus.skyColors.sunriseColor = "#FFDAB9";
        venus.skyColors.sunsetColor = "#FFCC99";
        venus.skyColors.dayColor = "#FFE4B5";
        venus.skyColors.nightColor = "#2F1E0F";
        venus.skyColors.horizonColor = "#FFC87C";
        config.planets.add(venus);

        // Earth
        VisualConfig.PlanetVisual earth = new VisualConfig.PlanetVisual();
        earth.name = "Earth";
        earth.forestFires.enabled = true;
        earth.forestFires.maxTintStrength = 0.7;
        earth.forestFires.spreadRadius = 5000.0;
        earth.forestFires.skyTintStart = "#FFAA00";
        earth.forestFires.skyTintEnd = "#FF4400";
        earth.forestFires.minDuration = 2.0;
        earth.forestFires.maxDuration = 10.0;
        earth.forestFires.minInterval = 50.0;
        earth.forestFires.maxInterval = 100.0;
        earth.skyColors.sunriseColor = "#FFA040";
        earth.skyColors.sunsetColor = "#FF6020";
        earth.skyColors.dayColor = "#87CEEB";
        earth.skyColors.nightColor = "#000814";
        earth.skyColors.horizonColor = "#FFB080";
        config.planets.add(earth);

        // Mars
        VisualConfig.PlanetVisual mars = new VisualConfig.PlanetVisual();
        mars.name = "Mars";
        mars.forestFires.enabled = false;
        mars.forestFires.maxTintStrength = 0;
        mars.forestFires.spreadRadius = 0;
        mars.forestFires.skyTintStart = "#FFFFFF";
        mars.forestFires.skyTintEnd = "#FFFFFF";
        mars.forestFires.minDuration = 0;
        mars.forestFires.maxDuration = 0;
        mars.forestFires.minInterval = 0;
        mars.forestFires.maxInterval = 0;
        mars.skyColors.sunriseColor = "#4080C0";
        mars.skyColors.sunsetColor = "#2060A0";
        mars.skyColors.dayColor = "#E8C9A6";
        mars.skyColors.nightColor = "#1A1410";
        mars.skyColors.horizonColor = "#C09060";
        config.planets.add(mars);

        // Jupiter
        VisualConfig.PlanetVisual jupiter = new VisualConfig.PlanetVisual();
        jupiter.name = "Jupiter";
        jupiter.forestFires.enabled = false;
        jupiter.forestFires.maxTintStrength = 0;
        jupiter.forestFires.spreadRadius = 0;
        jupiter.forestFires.skyTintStart = "#FFFFFF";
        jupiter.forestFires.skyTintEnd = "#FFFFFF";
        jupiter.forestFires.minDuration = 0;
        jupiter.forestFires.maxDuration = 0;
        jupiter.forestFires.minInterval = 0;
        jupiter.forestFires.maxInterval = 0;
        jupiter.skyColors.sunriseColor = "#FFD8FF";
        jupiter.skyColors.sunsetColor = "#FFA0FF";
        jupiter.skyColors.dayColor = "#FFCCFF";
        jupiter.skyColors.nightColor = "#1A001A";
        jupiter.skyColors.horizonColor = "#FFB0FF";
        config.planets.add(jupiter);

        // Saturn
        VisualConfig.PlanetVisual saturn = new VisualConfig.PlanetVisual();
        saturn.name = "Saturn";
        saturn.forestFires.enabled = false;
        saturn.forestFires.maxTintStrength = 0;
        saturn.forestFires.spreadRadius = 0;
        saturn.forestFires.skyTintStart = "#FFFFFF";
        saturn.forestFires.skyTintEnd = "#FFFFFF";
        saturn.forestFires.minDuration = 0;
        saturn.forestFires.maxDuration = 0;
        saturn.forestFires.minInterval = 0;
        saturn.forestFires.maxInterval = 0;
        saturn.skyColors.sunriseColor = "#D0E8FF";
        saturn.skyColors.sunsetColor = "#A0C8FF";
        saturn.skyColors.dayColor = "#C0E0FF";
        saturn.skyColors.nightColor = "#0A0010";
        saturn.skyColors.horizonColor = "#B0D0FF";
        config.planets.add(saturn);

        // Uranus
        VisualConfig.PlanetVisual uranus = new VisualConfig.PlanetVisual();
        uranus.name = "Uranus";
        uranus.forestFires.enabled = false;
        uranus.forestFires.maxTintStrength = 0;
        uranus.forestFires.spreadRadius = 0;
        uranus.forestFires.skyTintStart = "#FFFFFF";
        uranus.forestFires.skyTintEnd = "#FFFFFF";
        uranus.forestFires.minDuration = 0;
        uranus.forestFires.maxDuration = 0;
        uranus.forestFires.minInterval = 0;
        uranus.forestFires.maxInterval = 0;
        uranus.skyColors.sunriseColor = "#A0FFFF";
        uranus.skyColors.sunsetColor = "#80FFFF";
        uranus.skyColors.dayColor = "#B0FFFF";
        uranus.skyColors.nightColor = "#001020";
        uranus.skyColors.horizonColor = "#90FFFF";
        config.planets.add(uranus);

        // Neptune
        VisualConfig.PlanetVisual neptune = new VisualConfig.PlanetVisual();
        neptune.name = "Neptune";
        neptune.forestFires.enabled = false;
        neptune.forestFires.maxTintStrength = 0;
        neptune.forestFires.spreadRadius = 0;
        neptune.forestFires.skyTintStart = "#FFFFFF";
        neptune.forestFires.skyTintEnd = "#FFFFFF";
        neptune.forestFires.minDuration = 0;
        neptune.forestFires.maxDuration = 0;
        neptune.forestFires.minInterval = 0;
        neptune.forestFires.maxInterval = 0;
        neptune.skyColors.sunriseColor = "#809FFF";
        neptune.skyColors.sunsetColor = "#4060FF";
        neptune.skyColors.dayColor = "#6090FF";
        neptune.skyColors.nightColor = "#000810";
        neptune.skyColors.horizonColor = "#5070FF";
        config.planets.add(neptune);

        // Titan (moon)
        VisualConfig.MoonVisual titan = new VisualConfig.MoonVisual();
        titan.name = "Titan";
        titan.forestFires.enabled = false;
        titan.forestFires.maxTintStrength = 0;
        titan.forestFires.spreadRadius = 0;
        titan.forestFires.skyTintStart = "#FFFFFF";
        titan.forestFires.skyTintEnd = "#FFFFFF";
        titan.forestFires.minDuration = 0;
        titan.forestFires.maxDuration = 0;
        titan.forestFires.minInterval = 0;
        titan.forestFires.maxInterval = 0;
        titan.skyColors.sunriseColor = "#FFDDAA";
        titan.skyColors.sunsetColor = "#FFCC88";
        titan.skyColors.dayColor = "#FFE8AA";
        titan.skyColors.nightColor = "#1A0F0F";
        titan.skyColors.horizonColor = "#FFCC88";
        config.moons.add(titan);

        return config;
    }

    private static OrbitalConfig createDefaultOrbitalConfig() {
        OrbitalConfig config = new OrbitalConfig();

        // Mercury
        OrbitalConfig.PlanetOrbital mercury = new OrbitalConfig.PlanetOrbital();
        mercury.name = "Mercury";
        mercury.nodal = 325513.0;
        mercury.apsidal = 280000.0;
        mercury.axialPrecessionPeriod = 325513.0;
        mercury.minAxialTilt = 0.01;
        mercury.maxAxialTilt = 0.034;
        mercury.Day = 176.0;
        config.planets.add(mercury);

        // Venus
        OrbitalConfig.PlanetOrbital venus = new OrbitalConfig.PlanetOrbital();
        venus.name = "Venus";
        venus.nodal = 29000.0;
        venus.apsidal = 29000.0;
        venus.axialPrecessionPeriod = 0.0;
        venus.minAxialTilt = 2.64;
        venus.maxAxialTilt = 2.64;
        venus.Day = -116.75;
        config.planets.add(venus);

        // Earth
        OrbitalConfig.PlanetOrbital earth = new OrbitalConfig.PlanetOrbital();
        earth.name = "Earth";
        earth.nodal = 25772.0;
        earth.apsidal = 112000.0;
        earth.axialPrecessionPeriod = 25772.0;
        earth.minAxialTilt = 22.1;
        earth.maxAxialTilt = 24.5;
        earth.Day = 1.0;
        config.planets.add(earth);

        // Mars
        OrbitalConfig.PlanetOrbital mars = new OrbitalConfig.PlanetOrbital();
        mars.name = "Mars";
        mars.nodal = 170000.0;
        mars.apsidal = 7300.0;
        mars.axialPrecessionPeriod = 170000.0;
        mars.minAxialTilt = 22.04;
        mars.maxAxialTilt = 26.14;
        mars.Day = 1.027;
        config.planets.add(mars);

        // Jupiter
        OrbitalConfig.PlanetOrbital jupiter = new OrbitalConfig.PlanetOrbital();
        jupiter.name = "Jupiter";
        jupiter.nodal = 50687.0;
        jupiter.apsidal = 200000.0;
        jupiter.axialPrecessionPeriod = 0.0;
        jupiter.minAxialTilt = 3.13;
        jupiter.maxAxialTilt = 3.13;
        jupiter.Day = 0.41354;
        config.planets.add(jupiter);

        // Saturn
        OrbitalConfig.PlanetOrbital saturn = new OrbitalConfig.PlanetOrbital();
        saturn.name = "Saturn";
        saturn.nodal = 50687.0;
        saturn.apsidal = 1400000.0;
        saturn.axialPrecessionPeriod = 0.0;
        saturn.minAxialTilt = 26.73;
        saturn.maxAxialTilt = 26.73;
        saturn.Day = 0.44401;
        config.planets.add(saturn);

        // Uranus
        OrbitalConfig.PlanetOrbital uranus = new OrbitalConfig.PlanetOrbital();
        uranus.name = "Uranus";
        uranus.nodal = 0.0;
        uranus.apsidal = 0.0;
        uranus.axialPrecessionPeriod = 0.0;
        uranus.minAxialTilt = 82.23;
        uranus.maxAxialTilt = 82.23;
        uranus.Day = -0.71833;
        config.planets.add(uranus);

        // Neptune
        OrbitalConfig.PlanetOrbital neptune = new OrbitalConfig.PlanetOrbital();
        neptune.name = "Neptune";
        neptune.nodal = 0.0;
        neptune.apsidal = 0.0;
        neptune.axialPrecessionPeriod = 0.0;
        neptune.minAxialTilt = 28.32;
        neptune.maxAxialTilt = 28.32;
        neptune.Day = 0.67125;
        config.planets.add(neptune);

        // Pluto
        OrbitalConfig.PlanetOrbital pluto = new OrbitalConfig.PlanetOrbital();
        pluto.name = "Pluto";
        pluto.nodal = 20000.0;
        pluto.apsidal = 19951.0;
        pluto.axialPrecessionPeriod = 3000000.0;
        pluto.minAxialTilt = 102.0;
        pluto.maxAxialTilt = 126.0;
        pluto.Day = 6.4;
        config.planets.add(pluto);

        // MOONS

        // Moon (Earth's)
        OrbitalConfig.MoonOrbital moon = new OrbitalConfig.MoonOrbital();
        moon.name = "Moon";
        moon.axialPrecessionPeriod = 18.6;
        moon.minAxialTilt = 1.54;
        moon.maxAxialTilt = 1.54;
        moon.Day = 29.530589057;
        config.moons.add(moon);

        // Phobos (Mars)
        OrbitalConfig.MoonOrbital phobos = new OrbitalConfig.MoonOrbital();
        phobos.name = "Phobos";
        phobos.axialPrecessionPeriod = 2.262;
        phobos.minAxialTilt = 1.1;
        phobos.maxAxialTilt = 1.1;
        phobos.Day = 0.31891;
        config.moons.add(phobos);

        // Deimos (Mars)
        OrbitalConfig.MoonOrbital deimos = new OrbitalConfig.MoonOrbital();
        deimos.name = "Deimos";
        deimos.axialPrecessionPeriod = 54.537;
        deimos.minAxialTilt = 1.8;
        deimos.maxAxialTilt = 1.8;
        deimos.Day = 1.262361;
        config.moons.add(deimos);

        // Io (Jupiter)
        OrbitalConfig.MoonOrbital io = new OrbitalConfig.MoonOrbital();
        io.name = "Io";
        io.axialPrecessionPeriod = 1.333;
        io.minAxialTilt = 0.0;
        io.maxAxialTilt = 0.0;
        io.Day = 1.769861;
        config.moons.add(io);

        // Europa (Jupiter)
        OrbitalConfig.MoonOrbital europa = new OrbitalConfig.MoonOrbital();
        europa.name = "Europa";
        europa.axialPrecessionPeriod = 30.202;
        europa.minAxialTilt = 0.5;
        europa.maxAxialTilt = 0.5;
        europa.Day = 3.554095;
        config.moons.add(europa);

        // Ganymede (Jupiter)
        OrbitalConfig.MoonOrbital ganymede = new OrbitalConfig.MoonOrbital();
        ganymede.name = "Ganymede";
        ganymede.axialPrecessionPeriod = 137.812;
        ganymede.minAxialTilt = 0.2;
        ganymede.maxAxialTilt = 0.2;
        ganymede.Day = 7.166389;
        config.moons.add(ganymede);

        // Callisto (Jupiter)
        OrbitalConfig.MoonOrbital callisto = new OrbitalConfig.MoonOrbital();
        callisto.name = "Callisto";
        callisto.axialPrecessionPeriod = 577.264;
        callisto.minAxialTilt = 0.3;
        callisto.maxAxialTilt = 0.3;
        callisto.Day = 16.753563;
        config.moons.add(callisto);

        // Mimas (Saturn)
        OrbitalConfig.MoonOrbital mimas = new OrbitalConfig.MoonOrbital();
        mimas.name = "Mimas";
        mimas.axialPrecessionPeriod = 0.0;
        mimas.minAxialTilt = 0.0;
        mimas.maxAxialTilt = 0.0;
        mimas.Day = 0.94;
        config.moons.add(mimas);

        // Enceladus (Saturn)
        OrbitalConfig.MoonOrbital enceladus = new OrbitalConfig.MoonOrbital();
        enceladus.name = "Enceladus";
        enceladus.axialPrecessionPeriod = 0.0;
        enceladus.minAxialTilt = 0.0;
        enceladus.maxAxialTilt = 0.0;
        enceladus.Day = 1.37;
        config.moons.add(enceladus);

        // Tethys (Saturn)
        OrbitalConfig.MoonOrbital tethys = new OrbitalConfig.MoonOrbital();
        tethys.name = "Tethys";
        tethys.axialPrecessionPeriod = 0.0;
        tethys.minAxialTilt = 0.0;
        tethys.maxAxialTilt = 0.0;
        tethys.Day = 1.89;
        config.moons.add(tethys);

        // Dione (Saturn)
        OrbitalConfig.MoonOrbital dione = new OrbitalConfig.MoonOrbital();
        dione.name = "Dione";
        dione.axialPrecessionPeriod = 0.0;
        dione.minAxialTilt = 0.0;
        dione.maxAxialTilt = 0.0;
        dione.Day = 2.73;
        config.moons.add(dione);

        // Rhea (Saturn)
        OrbitalConfig.MoonOrbital rhea = new OrbitalConfig.MoonOrbital();
        rhea.name = "Rhea";
        rhea.axialPrecessionPeriod = 0.0;
        rhea.minAxialTilt = 0.0;
        rhea.maxAxialTilt = 0.0;
        rhea.Day = 4.52;
        config.moons.add(rhea);

        // Titan (Saturn)
        OrbitalConfig.MoonOrbital titan = new OrbitalConfig.MoonOrbital();
        titan.name = "Titan";
        titan.axialPrecessionPeriod = 0.0;
        titan.minAxialTilt = 0.33;
        titan.maxAxialTilt = 0.33;
        titan.Day = 15.95;
        config.moons.add(titan);

        // Hyperion (Saturn)
        OrbitalConfig.MoonOrbital hyperion = new OrbitalConfig.MoonOrbital();
        hyperion.name = "Hyperion";
        hyperion.axialPrecessionPeriod = 0.0;
        hyperion.minAxialTilt = 0.43;
        hyperion.maxAxialTilt = 0.43;
        hyperion.Day = 21.28;
        config.moons.add(hyperion);

        // Iapetus (Saturn)
        OrbitalConfig.MoonOrbital iapetus = new OrbitalConfig.MoonOrbital();
        iapetus.name = "Iapetus";
        iapetus.axialPrecessionPeriod = 0.0;
        iapetus.minAxialTilt = 15.47;
        iapetus.maxAxialTilt = 15.47;
        iapetus.Day = 79.32;
        config.moons.add(iapetus);

        // Ariel (Uranus)
        OrbitalConfig.MoonOrbital ariel = new OrbitalConfig.MoonOrbital();
        ariel.name = "Ariel";
        ariel.axialPrecessionPeriod = 0.0;
        ariel.minAxialTilt = 0.26;
        ariel.maxAxialTilt = 0.26;
        ariel.Day = 2.52;
        config.moons.add(ariel);

        // Umbriel (Uranus)
        OrbitalConfig.MoonOrbital umbriel = new OrbitalConfig.MoonOrbital();
        umbriel.name = "Umbriel";
        umbriel.axialPrecessionPeriod = 0.0;
        umbriel.minAxialTilt = 0.36;
        umbriel.maxAxialTilt = 0.36;
        umbriel.Day = 4.14;
        config.moons.add(umbriel);

        // Titania (Uranus)
        OrbitalConfig.MoonOrbital titania = new OrbitalConfig.MoonOrbital();
        titania.name = "Titania";
        titania.axialPrecessionPeriod = 0.0;
        titania.minAxialTilt = 0.08;
        titania.maxAxialTilt = 0.08;
        titania.Day = 8.71;
        config.moons.add(titania);

        // Oberon (Uranus)
        OrbitalConfig.MoonOrbital oberon = new OrbitalConfig.MoonOrbital();
        oberon.name = "Oberon";
        oberon.axialPrecessionPeriod = 0.0;
        oberon.minAxialTilt = 0.1;
        oberon.maxAxialTilt = 0.1;
        oberon.Day = 13.46;
        config.moons.add(oberon);

        // Miranda (Uranus)
        OrbitalConfig.MoonOrbital miranda = new OrbitalConfig.MoonOrbital();
        miranda.name = "Miranda";
        miranda.axialPrecessionPeriod = 0.0;
        miranda.minAxialTilt = 4.22;
        miranda.maxAxialTilt = 4.22;
        miranda.Day = 1.41;
        config.moons.add(miranda);

        // Triton (Neptune)
        OrbitalConfig.MoonOrbital triton = new OrbitalConfig.MoonOrbital();
        triton.name = "Triton";
        triton.axialPrecessionPeriod = 0.0;
        triton.minAxialTilt = 23.0;
        triton.maxAxialTilt = 23.0;
        triton.Day = 5.88;
        config.moons.add(triton);

        // Charon (Pluto)
        OrbitalConfig.MoonOrbital charon = new OrbitalConfig.MoonOrbital();
        charon.name = "Charon";
        charon.axialPrecessionPeriod = 0.0;
        charon.minAxialTilt = 0.0;
        charon.maxAxialTilt = 0.0;
        charon.Day = 6.4;
        config.moons.add(charon);

        return config;
    }
}