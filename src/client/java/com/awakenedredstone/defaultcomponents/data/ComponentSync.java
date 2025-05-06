package com.awakenedredstone.defaultcomponents.data;

import com.awakenedredstone.defaultcomponents.network.SyncPayload;

import java.util.Map;

public class ComponentSync {
    public static void sync(SyncPayload payload) {
        DefaultComponentData loader = DefaultComponentData.INSTANCE;
        loader.modComponents = payload.globalComponents();
        loader.itemComponents = payload.itemComponents();
        loader.modifyItems();
    }

    public static void unload() {
        DefaultComponentData loader = DefaultComponentData.INSTANCE;
        loader.modComponents = Map.of();
        loader.itemComponents = Map.of();
        loader.modifyItems();
    }
}
