package com.awakenedredstone.defaultcomponents.component;

import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RuntimeComponentMap implements ComponentMap {
    private ComponentMap map;

    public RuntimeComponentMap(ComponentMap originalMap) {
        this.map = originalMap;
    }

    public void setMap(ComponentMap map) {
        this.map = map;
    }

    public ComponentMap getMap() {
        return map;
    }

    @Override
    public @Nullable <T> T get(ComponentType<? extends T> type) {
        return map.get(type);
    }

    @Override
    public Set<ComponentType<?>> getTypes() {
        return map.getTypes();
    }

    @Override
    public boolean contains(ComponentType<?> type) {
        return map.contains(type);
    }

    @Override
    public <T> T getOrDefault(ComponentType<? extends T> type, T fallback) {
        return map.getOrDefault(type, fallback);
    }

    //? if <1.21.5 {
    /*@Override
    public @Nullable <T> Component<T> copy(ComponentType<T> type) {
        return map.copy(type);
    }
    *///?}

    @Override
    public @NotNull Iterator<Component<?>> iterator() {
        return map.iterator();
    }

    @Override
    public Stream<Component<?>> stream() {
        return map.stream();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public ComponentMap filtered(Predicate<ComponentType<?>> predicate) {
        return map.filtered(predicate);
    }
}
