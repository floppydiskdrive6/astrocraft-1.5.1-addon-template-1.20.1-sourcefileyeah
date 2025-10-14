package net.witherstorm8475.astrocraftaddon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static VisualConfig visualConfig = new VisualConfig();
    public static OrbitalConfig orbitalConfig = new OrbitalConfig();

    public static void load(File visualFile, File orbitalFile) {
        try (Reader r1 = new FileReader(visualFile)) {
            visualConfig = GSON.fromJson(r1, VisualConfig.class);
        } catch (IOException e) { e.printStackTrace(); }

        try (Reader r2 = new FileReader(orbitalFile)) {
            orbitalConfig = GSON.fromJson(r2, OrbitalConfig.class);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void save(File visualFile, File orbitalFile) {
        try (Writer w1 = new FileWriter(visualFile)) {
            GSON.toJson(visualConfig, w1);
        } catch (IOException e) { e.printStackTrace(); }

        try (Writer w2 = new FileWriter(orbitalFile)) {
            GSON.toJson(orbitalConfig, w2);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
