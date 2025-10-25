package net.tysontheember.emberstextapi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.options.ETAOptions;

@Mixin(SimpleOptionsSubScreen.class)
public abstract class SimpleOptionsSubScreenMixin extends OptionsSubScreen {
    @Shadow
    protected OptionsList list;

    protected SimpleOptionsSubScreenMixin(Screen parent, net.minecraft.client.Options options, Component title) {
        super(parent, options, title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V", shift = At.Shift.AFTER))
    private void emberstextapi$appendETAOptions(CallbackInfo ci) {
        if (!((Object) this instanceof ChatOptionsScreen)) {
            return;
        }
        ETAOptions options = GlobalTextConfig.getClientOptions();
        if (options == null) {
            return;
        }
        OptionInstance<Boolean> animation = options.animationEnabledOption();
        OptionInstance<?> mode = options.typewriterModeOption();
        this.list.addSmall(animation, mode);
        this.list.addSmall(new OptionInstance<?>[]{options.typewriterSpeedOption()});
    }
}
