package com.awakenedredstone.defaultcomponents.mixin;

/*? if <=1.21.1 {*/
/*import net.minecraft.registry.tag.TagManagerLoader;
*//*?}*/
import net.minecraft.server.DataPackContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataPackContents.class)
public interface DataPackContentsAccessor {
    /*? if <=1.21.1 {*/
    /*@Accessor TagManagerLoader getRegistryTagManager();
    *//*?}*/
}
