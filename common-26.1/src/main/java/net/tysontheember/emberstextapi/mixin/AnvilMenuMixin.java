package net.tysontheember.emberstextapi.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @ModifyConstant(method = "validateName", constant = @Constant(intValue = 50))
    private static int emberstextapi$modifyAnvilNameLimit(int original) {
        return ConfigHelper.getInstance().getAnvilNameMaxLength();
    }
}
