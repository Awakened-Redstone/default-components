package com.awakenedredstone.defaultcomponents.duck;

import com.awakenedredstone.defaultcomponents.data.DefaultComponentLoader;
import net.minecraft.component.ComponentMap;

public interface ModifyDefaultComponents {
    void defaultComponents$modifyComponents(DefaultComponentLoader.ComponentManipulation components);
    ComponentMap defaultComponents$defaultComponents();
}
