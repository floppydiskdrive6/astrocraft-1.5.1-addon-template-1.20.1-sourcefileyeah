package net.witherstorm8475.astrocraftaddon.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.File;

public class AstrocraftAddonConfigScreenFactory {

    public static Screen create(Screen parent, File visualFile, File orbitalFile) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("AstroCraft Addon Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // --- Visual Config ---
        for (VisualConfig.PlanetVisual planet : ConfigManager.visualConfig.planets) {
            ConfigCategory planetCat = builder.getOrCreateCategory(Text.of("Planet: " + planet.name));

            // Forest Fires
            planetCat.addEntry(
                    entryBuilder.startBooleanToggle(Text.of("Forest Fires Enabled"), planet.forestFires.enabled)
                            .setSaveConsumer(val -> planet.forestFires.enabled = val)
                            .build()
            );
            planetCat.addEntry(
                    entryBuilder.startDoubleField(Text.of("Max Tint Strength"), planet.forestFires.maxTintStrength)
                            .setSaveConsumer(val -> planet.forestFires.maxTintStrength = val)
                            .build()
            );

            // Sky Colors (as Strings)
            planetCat.addEntry(
                    entryBuilder.startTextField(Text.of("Sunrise Color"), planet.skyColors.sunriseColor)
                            .setSaveConsumer(val -> planet.skyColors.sunriseColor = val)
                            .build()
            );
            planetCat.addEntry(
                    entryBuilder.startTextField(Text.of("Sunset Color"), planet.skyColors.sunsetColor)
                            .setSaveConsumer(val -> planet.skyColors.sunsetColor = val)
                            .build()
            );
            planetCat.addEntry(
                    entryBuilder.startTextField(Text.of("Day Color"), planet.skyColors.dayColor)
                            .setSaveConsumer(val -> planet.skyColors.dayColor = val)
                            .build()
            );
            planetCat.addEntry(
                    entryBuilder.startTextField(Text.of("Night Color"), planet.skyColors.nightColor)
                            .setSaveConsumer(val -> planet.skyColors.nightColor = val)
                            .build()
            );
            planetCat.addEntry(
                    entryBuilder.startTextField(Text.of("Horizon Color"), planet.skyColors.horizonColor)
                            .setSaveConsumer(val -> planet.skyColors.horizonColor = val)
                            .build()
            );
        }

        for (VisualConfig.MoonVisual moon : ConfigManager.visualConfig.moons) {
            ConfigCategory moonCat = builder.getOrCreateCategory(Text.of("Moon: " + moon.name));

            // Forest Fires
            moonCat.addEntry(
                    entryBuilder.startBooleanToggle(Text.of("Forest Fires Enabled"), moon.forestFires.enabled)
                            .setSaveConsumer(val -> moon.forestFires.enabled = val)
                            .build()
            );

            // Sky Colors (as Strings)
            moonCat.addEntry(
                    entryBuilder.startTextField(Text.of("Sunrise Color"), moon.skyColors.sunriseColor)
                            .setSaveConsumer(val -> moon.skyColors.sunriseColor = val)
                            .build()
            );
            moonCat.addEntry(
                    entryBuilder.startTextField(Text.of("Sunset Color"), moon.skyColors.sunsetColor)
                            .setSaveConsumer(val -> moon.skyColors.sunsetColor = val)
                            .build()
            );
            moonCat.addEntry(
                    entryBuilder.startTextField(Text.of("Day Color"), moon.skyColors.dayColor)
                            .setSaveConsumer(val -> moon.skyColors.dayColor = val)
                            .build()
            );
            moonCat.addEntry(
                    entryBuilder.startTextField(Text.of("Night Color"), moon.skyColors.nightColor)
                            .setSaveConsumer(val -> moon.skyColors.nightColor = val)
                            .build()
            );
            moonCat.addEntry(
                    entryBuilder.startTextField(Text.of("Horizon Color"), moon.skyColors.horizonColor)
                            .setSaveConsumer(val -> moon.skyColors.horizonColor = val)
                            .build()
            );
        }

        // --- Orbital Config ---
        for (OrbitalConfig.PlanetOrbital planet : ConfigManager.orbitalConfig.planets) {
            ConfigCategory planetCat = builder.getOrCreateCategory(Text.of("Orbital: " + planet.name));

            planetCat.addEntry(
                    entryBuilder.startDoubleField(Text.of("Min Axial Tilt"), planet.minAxialTilt)
                            .setSaveConsumer(val -> planet.minAxialTilt = val)
                            .build()
            );
            planetCat.addEntry(
                    entryBuilder.startDoubleField(Text.of("Max Axial Tilt"), planet.maxAxialTilt)
                            .setSaveConsumer(val -> planet.maxAxialTilt = val)
                            .build()
            );
            planetCat.addEntry(
                    entryBuilder.startDoubleField(Text.of("Day Length"), planet.Day)
                            .setSaveConsumer(val -> planet.Day = val)
                            .build()
            );
        }

        for (OrbitalConfig.MoonOrbital moon : ConfigManager.orbitalConfig.moons) {
            ConfigCategory moonCat = builder.getOrCreateCategory(Text.of("Orbital: " + moon.name));

            moonCat.addEntry(
                    entryBuilder.startDoubleField(Text.of("Min Axial Tilt"), moon.minAxialTilt)
                            .setSaveConsumer(val -> moon.minAxialTilt = val)
                            .build()
            );
            moonCat.addEntry(
                    entryBuilder.startDoubleField(Text.of("Max Axial Tilt"), moon.maxAxialTilt)
                            .setSaveConsumer(val -> moon.maxAxialTilt = val)
                            .build()
            );
            moonCat.addEntry(
                    entryBuilder.startDoubleField(Text.of("Day Length"), moon.Day)
                            .setSaveConsumer(val -> moon.Day = val)
                            .build()
            );
        }

        // Save changes automatically when screen closes
        builder.setSavingRunnable(() -> ConfigManager.save(visualFile, orbitalFile));

        return builder.build();
    }
}
