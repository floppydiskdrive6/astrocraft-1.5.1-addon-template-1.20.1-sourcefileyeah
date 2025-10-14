package net.witherstorm8475.astrocraftaddon.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.io.File;

public class AddonModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AstrocraftAddonConfigScreenFactory.create(
                parent,
                new File("config/astrocraft-addon-visual.json"),
                new File("config/astrocraft-addon-orbital.json")
        );
    }
}
