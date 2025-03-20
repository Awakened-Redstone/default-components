package com.awakenedredstone.defaultcomponents.data;

import com.awakenedredstone.defaultcomponents.duck.ModifyDefaultComponents;
import com.awakenedredstone.defaultcomponents.network.SyncPayload;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.DataPackContents;
import net.minecraft.util.Identifier;

import java.util.Map;
/*? if >=1.21.2 {*/
import net.minecraft.registry.tag.TagGroupLoader;
import java.util.ArrayList;
import java.util.List;
/*?}*/

public class DefaultComponentData {
    public static final DefaultComponentData INSTANCE = new DefaultComponentData();

    /*? if >=1.21.2 {*/
    public List<TagGroupLoader.RegistryTags<?>> registryTags = new ArrayList<>();
    /*?}*/
    public DataPackContents dataPackContents;

    Map<String, DefaultComponentLoader.ComponentManipulation> modComponents = Map.of();
    Map<Identifier, DefaultComponentLoader.ComponentManipulation> itemComponents = Map.of();

    public Map<String, DefaultComponentLoader.ComponentManipulation> getModComponents() {
        return modComponents;
    }

    public Map<Identifier, DefaultComponentLoader.ComponentManipulation> getItemComponents() {
        return itemComponents;
    }

    void modifyItems() {
        if (!modComponents.isEmpty()) {
            //TODO: Improve processing time
            for (Item item : Registries.ITEM) {
                if (item instanceof ModifyDefaultComponents modifiable) {
                    Identifier id = Registries.ITEM.getId(item);
                    modifiable.defaultComponents$modifyComponents(getItemComponents().get(id), getModComponents().get(id.getNamespace()));
                }
            }
        } else {
            itemComponents.forEach((identifier, componentManipulation) -> {
                //? if >=1.21.2 {
                Item item = Registries.ITEM.getOptionalValue(identifier).orElse(null);
                //?} else {
                /*Item item = Registries.ITEM.getOrEmpty(identifier).orElse(null);
                *///?}
                if (item instanceof ModifyDefaultComponents modifiable) {
                    modifiable.defaultComponents$modifyComponents(getItemComponents().get(identifier), null);
                }
            });
        }
    }

    public static SyncPayload createSyncPayload() {
        return new SyncPayload(INSTANCE.modComponents, INSTANCE.getItemComponents());
    }
}
