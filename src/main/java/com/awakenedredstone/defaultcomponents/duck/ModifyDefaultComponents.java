package com.awakenedredstone.defaultcomponents.duck;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import net.minecraft.component.ComponentMap;
import org.jetbrains.annotations.Nullable;

public interface ModifyDefaultComponents {
    void defaultComponents$modifyComponents(@Nullable DefaultComponentLoader.ComponentManipulation itemComponents, @Nullable DefaultComponentLoader.ComponentManipulation modComponents);
    ComponentMap defaultComponents$defaultComponents();
}
