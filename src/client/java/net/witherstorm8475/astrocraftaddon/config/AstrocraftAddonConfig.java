package net.witherstorm8475.astrocraftaddon.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class AstrocraftAddonConfig {

    public static void init() {
        AutoConfig.register(ConfigManager.class, GsonConfigSerializer::new);
    }

    public static ConfigManager getConfig() {
        return AutoConfig.getConfigHolder(ConfigManager.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(ConfigManager.class).save();
    }

    public static void load() {
        AutoConfig.getConfigHolder(ConfigManager.class).load();
    }

    public static void reload() {
        load();
    }
}