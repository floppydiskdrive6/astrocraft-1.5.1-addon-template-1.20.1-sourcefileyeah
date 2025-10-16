package net.witherstorm8475.astrocraftaddon.config;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screen.Screen;

public class AstrocraftAddonConfigScreenFactory {

    public static Screen getMainScreen(Screen parent) {
        return AutoConfig.getConfigScreen(ConfigManager.class, parent).get();
    }
}