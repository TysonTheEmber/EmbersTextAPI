package net.tysontheember.emberstextapi.text;

import net.minecraft.resources.ResourceLocation;

public interface TextAttributeFactory {
    ResourceLocation id();

    ParamSpec spec();

    TextEffect compile(Params params, CompileContext context) throws CompileException;
}
