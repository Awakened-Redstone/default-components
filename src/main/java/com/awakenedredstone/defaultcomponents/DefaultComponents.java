package com.awakenedredstone.defaultcomponents;

import com.awakenedredstone.defaultcomponents.config.DefaultComponentsConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultComponents implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Default Components");
    public static final DefaultComponentsConfig CONFIG = new DefaultComponentsConfig();

    @Override
    public void onInitialize() {
        //TODO: Make packets to trick vanilla clients
        //TODO: Somehow make it reloadable at runtime so servers can send their config to the client
    }

    static {
        CONFIG.load();
    }
}
