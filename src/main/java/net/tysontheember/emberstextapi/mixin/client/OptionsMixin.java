package net.tysontheember.emberstextapi.mixin.client;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.options.ETAOptions;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Unique
    private ETAOptions emberstextapi$etaOptions;

    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At("TAIL"))
    private void emberstextapi$init(Minecraft minecraft, File file, CallbackInfo ci) {
        this.emberstextapi$etaOptions = new ETAOptions(GlobalTextConfig.getOptions());
        GlobalTextConfig.setClientOptions(this.emberstextapi$etaOptions);
        GlobalTextConfig.setOptions(this.emberstextapi$etaOptions.snapshot());
    }

    @Inject(method = "processOptions(Lnet/minecraft/client/Options$FieldAccess;)V", at = @At("TAIL"))
    private void emberstextapi$process(Options.FieldAccess access, CallbackInfo ci) {
        if (this.emberstextapi$etaOptions == null) {
            this.emberstextapi$etaOptions = new ETAOptions(GlobalTextConfig.getOptions());
            GlobalTextConfig.setClientOptions(this.emberstextapi$etaOptions);
        }
        this.emberstextapi$etaOptions.process(access);
        GlobalTextConfig.setOptions(this.emberstextapi$etaOptions.snapshot());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void emberstextapi$syncAfterLoad(CallbackInfo ci) {
        if (this.emberstextapi$etaOptions != null) {
            this.emberstextapi$etaOptions.loadFromSnapshot(GlobalTextConfig.getOptions());
        }
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void emberstextapi$saveSync(CallbackInfo ci) {
        if (this.emberstextapi$etaOptions != null) {
            GlobalTextConfig.setOptions(this.emberstextapi$etaOptions.snapshot());
        }
    }
}
