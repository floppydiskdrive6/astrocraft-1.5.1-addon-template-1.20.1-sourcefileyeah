package net.witherstorm8475.astrocraftaddon.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;

public class AstrocraftAddonConfigScreenFactory {

    public static Screen create(Screen parent) {
        // Load configs and initialize if null
        ConfigManager.load();
        if (ConfigManager.visualConfig == null) ConfigManager.visualConfig = new VisualConfig();
        if (ConfigManager.orbitalConfig == null) ConfigManager.orbitalConfig = new OrbitalConfig();
        if (ConfigManager.visualConfig.planets == null) ConfigManager.visualConfig.planets = new ArrayList<>();
        if (ConfigManager.visualConfig.moons == null) ConfigManager.visualConfig.moons = new ArrayList<>();
        if (ConfigManager.orbitalConfig.planets == null) ConfigManager.orbitalConfig.planets = new ArrayList<>();
        if (ConfigManager.orbitalConfig.moons == null) ConfigManager.orbitalConfig.moons = new ArrayList<>();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("AstroCraft Addon Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        boolean hasEntries = false;

// ==========================
//       VISUAL CONFIG
// ==========================
        ConfigCategory visualCat = builder.getOrCreateCategory(Text.of("Visual Config"));

// --------------------------
// Planets
// --------------------------
        for (VisualConfig.PlanetVisual planet : ConfigManager.visualConfig.planets) {
            if (planet == null) continue;
            hasEntries = true;

            // Planet header
            visualCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§e" + planet.name.toUpperCase())).build());

            // Editable Planet Name
            visualCat.addEntry(entryBuilder.startStrField(Text.of("Planet Name"), planet.name)
                    .setSaveConsumer(val -> planet.name = val)
                    .build());

            // Initialize forest fires and sky colors if null
            if (planet.forestFires == null) planet.forestFires = new VisualConfig.PlanetVisual.ForestFire();
            if (planet.skyColors == null) planet.skyColors = new VisualConfig.PlanetVisual.SkyColors();

            // Forest Fires
            visualCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§6  Forest Fires")).build());
            visualCat.addEntry(entryBuilder.startBooleanToggle(Text.of("    Enable"), planet.forestFires.enabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(val -> planet.forestFires.enabled = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Tint Strength"), planet.forestFires.maxTintStrength)
                    .setDefaultValue(0.7).setMin(0.0).setMax(1.0)
                    .setSaveConsumer(val -> planet.forestFires.maxTintStrength = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Spread Radius"), planet.forestFires.spreadRadius)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> planet.forestFires.spreadRadius = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sky Tint Start"), planet.forestFires.skyTintStart)
                    .setDefaultValue("#FFAA00")
                    .setSaveConsumer(val -> planet.forestFires.skyTintStart = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sky Tint End"), planet.forestFires.skyTintEnd)
                    .setDefaultValue("#FF4400")
                    .setSaveConsumer(val -> planet.forestFires.skyTintEnd = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Min Duration"), planet.forestFires.minDuration)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> planet.forestFires.minDuration = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Duration"), planet.forestFires.maxDuration)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> planet.forestFires.maxDuration = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Min Interval"), planet.forestFires.minInterval)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> planet.forestFires.minInterval = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Interval"), planet.forestFires.maxInterval)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> planet.forestFires.maxInterval = val)
                    .build());

            // Sky Colors
            visualCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§9  Sky Colors")).build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sunrise Color"), planet.skyColors.sunriseColor)
                    .setDefaultValue("#FFA040")
                    .setSaveConsumer(val -> planet.skyColors.sunriseColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sunset Color"), planet.skyColors.sunsetColor)
                    .setDefaultValue("#FF6020")
                    .setSaveConsumer(val -> planet.skyColors.sunsetColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Day Color"), planet.skyColors.dayColor)
                    .setDefaultValue("#87CEEB")
                    .setSaveConsumer(val -> planet.skyColors.dayColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Night Color"), planet.skyColors.nightColor)
                    .setDefaultValue("#000814")
                    .setSaveConsumer(val -> planet.skyColors.nightColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Horizon Color"), planet.skyColors.horizonColor)
                    .setDefaultValue("#FFB080")
                    .setSaveConsumer(val -> planet.skyColors.horizonColor = val)
                    .build());
        }

// --------------------------
// Moons
// --------------------------
        for (VisualConfig.MoonVisual moon : ConfigManager.visualConfig.moons) {
            if (moon == null) continue;
            hasEntries = true;

            // Moon header
            visualCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§b" + moon.name.toUpperCase())).build());

            // Editable Moon Name
            visualCat.addEntry(entryBuilder.startStrField(Text.of("Moon Name"), moon.name)
                    .setSaveConsumer(val -> moon.name = val)
                    .build());

            // Initialize forest fires and sky colors if null
            if (moon.forestFires == null) moon.forestFires = new VisualConfig.PlanetVisual.ForestFire();
            if (moon.skyColors == null) moon.skyColors = new VisualConfig.PlanetVisual.SkyColors();

            // Forest Fires
            visualCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§6  Forest Fires")).build());
            visualCat.addEntry(entryBuilder.startBooleanToggle(Text.of("    Enable"), moon.forestFires.enabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(val -> moon.forestFires.enabled = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Tint Strength"), moon.forestFires.maxTintStrength)
                    .setDefaultValue(0.7).setMin(0.0).setMax(1.0)
                    .setSaveConsumer(val -> moon.forestFires.maxTintStrength = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Spread Radius"), moon.forestFires.spreadRadius)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> moon.forestFires.spreadRadius = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sky Tint Start"), moon.forestFires.skyTintStart)
                    .setDefaultValue("#FFAA00")
                    .setSaveConsumer(val -> moon.forestFires.skyTintStart = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sky Tint End"), moon.forestFires.skyTintEnd)
                    .setDefaultValue("#FF4400")
                    .setSaveConsumer(val -> moon.forestFires.skyTintEnd = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Min Duration"), moon.forestFires.minDuration)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> moon.forestFires.minDuration = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Duration"), moon.forestFires.maxDuration)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> moon.forestFires.maxDuration = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Min Interval"), moon.forestFires.minInterval)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> moon.forestFires.minInterval = val)
                    .build());
            visualCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Interval"), moon.forestFires.maxInterval)
                    .setDefaultValue(5000.0).setMin(0.0)
                    .setSaveConsumer(val -> moon.forestFires.maxInterval = val)
                    .build());

            // Sky Colors
            visualCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§9  Sky Colors")).build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sunrise Color"), moon.skyColors.sunriseColor)
                    .setDefaultValue("#FFA040")
                    .setSaveConsumer(val -> moon.skyColors.sunriseColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Sunset Color"), moon.skyColors.sunsetColor)
                    .setDefaultValue("#FF6020")
                    .setSaveConsumer(val -> moon.skyColors.sunsetColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Day Color"), moon.skyColors.dayColor)
                    .setDefaultValue("#87CEEB")
                    .setSaveConsumer(val -> moon.skyColors.dayColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Night Color"), moon.skyColors.nightColor)
                    .setDefaultValue("#000814")
                    .setSaveConsumer(val -> moon.skyColors.nightColor = val)
                    .build());
            visualCat.addEntry(entryBuilder.startStrField(Text.of("    Horizon Color"), moon.skyColors.horizonColor)
                    .setDefaultValue("#FFB080")
                    .setSaveConsumer(val -> moon.skyColors.horizonColor = val)
                    .build());
        }


// ==========================
//       ORBITAL CONFIG
// ==========================
        ConfigCategory orbitalCat = builder.getOrCreateCategory(Text.of("Orbital Config"));

// Loop through orbital planets
        for (int i = 0; i < ConfigManager.orbitalConfig.planets.size(); i++) {
            int index = i;
            OrbitalConfig.PlanetOrbital planet = ConfigManager.orbitalConfig.planets.get(i);
            if (planet == null || planet.name == null) continue;
            hasEntries = true;

            orbitalCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§e" + planet.name.toUpperCase())).build());

            // Editable Planet Name
            orbitalCat.addEntry(entryBuilder.startStrField(Text.of("Planet Name"), planet.name)
                    .setSaveConsumer(val -> planet.name = val)
                    .build());

            // Axial Tilt & Precession
            orbitalCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§a  Axial Tilt & Precession")).build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Min Axial Tilt"), planet.minAxialTilt)
                    .setSaveConsumer(val -> planet.minAxialTilt = val)
                    .build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Axial Tilt"), planet.maxAxialTilt)
                    .setSaveConsumer(val -> planet.maxAxialTilt = val)
                    .build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Axial Precession Period"), planet.axialPrecessionPeriod)
                    .setSaveConsumer(val -> planet.axialPrecessionPeriod = val)
                    .build());

            // Rotation
            orbitalCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§e  Rotation")).build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Day Length"), planet.Day)
                    .setSaveConsumer(val -> planet.Day = val)
                    .build());

            // Orbital Precession
            orbitalCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§d  Orbital Precession")).build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Nodal Precession"), planet.nodal)
                    .setSaveConsumer(val -> planet.nodal = val)
                    .build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Apsidal Precession"), planet.apsidal)
                    .setSaveConsumer(val -> planet.apsidal = val)
                    .build());
        }

// Loop through orbital moons

        for (int i = 0; i < ConfigManager.orbitalConfig.moons.size(); i++) {
            int index = i;
            OrbitalConfig.MoonOrbital moon = ConfigManager.orbitalConfig.moons.get(i);
            if (moon == null || moon.name == null) continue;
            hasEntries = true;

            orbitalCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§b" + moon.name.toUpperCase())).build());

            // Editable Moon Name
            orbitalCat.addEntry(entryBuilder.startStrField(Text.of("Moon Name"), moon.name)
                    .setSaveConsumer(val -> moon.name = val)
                    .build());

            // Axial Tilt & Precession
            orbitalCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§a  Axial Tilt & Precession")).build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Min Axial Tilt"), moon.minAxialTilt)
                    .setSaveConsumer(val -> moon.minAxialTilt = val)
                    .build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Max Axial Tilt"), moon.maxAxialTilt)
                    .setSaveConsumer(val -> moon.maxAxialTilt = val)
                    .build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Axial Precession Period"), moon.axialPrecessionPeriod)
                    .setSaveConsumer(val -> moon.axialPrecessionPeriod = val)
                    .build());

            // Rotation
            orbitalCat.addEntry(entryBuilder.startTextDescription(Text.of("§l§e  Rotation")).build());
            orbitalCat.addEntry(entryBuilder.startDoubleField(Text.of("    Day Length"), moon.Day)
                    .setSaveConsumer(val -> moon.Day = val)
                    .build());
        }

        // ==========================
        //       SAVE + FALLBACK
        // ==========================
        builder.setSavingRunnable(() -> {
            ConfigManager.save();
            System.out.println("[AstroCraft Addon] Config saved!");
        });

        // Fallback if no entries
        if (!hasEntries) {
            ConfigCategory fallback = builder.getOrCreateCategory(Text.of("Info"));
            fallback.addEntry(entryBuilder.startTextDescription(Text.of("No planets or moons found in config!")).build());
            fallback.addEntry(entryBuilder.startTextDescription(Text.of("Check: config/astrocraft-151-addon/")).build());
        }

        return builder.build();
    }
}
