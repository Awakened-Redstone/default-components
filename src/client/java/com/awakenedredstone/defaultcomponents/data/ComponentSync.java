package com.awakenedredstone.defaultcomponents.data;

import com.awakenedredstone.defaultcomponents.DefaultComponents;
import com.awakenedredstone.defaultcomponents.duck.RebuildDefaultComponents;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;

import java.util.Map;

public class ComponentSync {
    public static void sync(SyncPayload payload) {
        DefaultComponentLoader loader = DefaultComponentLoader.INSTANCE;
        loader.globalComponents = payload.globalComponents();
        loader.itemComponents = payload.itemComponents();
        loader.modifyItems();
        synchronized (DefaultComponents.ITEM_STACKS) {
            DefaultComponents.ITEM_STACKS.forEach(RebuildDefaultComponents::defaultComponents$rebuildComponents);
        }
    }

    public static void unload() {
        DefaultComponentLoader loader = DefaultComponentLoader.INSTANCE;
        loader.globalComponents = DefaultComponentLoader.ComponentManipulation.EMPTY;
        loader.itemComponents = Map.of();
        loader.modifyItems();
    }
}
