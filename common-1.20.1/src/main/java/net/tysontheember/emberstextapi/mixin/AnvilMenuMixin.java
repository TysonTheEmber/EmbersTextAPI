package net.tysontheember.emberstextapi.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Extends the anvil rename length limit in AnvilMenu.
 * Uses require=0 as the method structure may differ in 1.20.1.
 */
@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @ModifyConstant(method = "setItemName", constant = @Constant(intValue = 50), require = 0)
    private int emberstextapi$modifyAnvilNameLimit(int original) {
        return ConfigHelper.getInstance().getAnvilNameMaxLength();
    }
}
