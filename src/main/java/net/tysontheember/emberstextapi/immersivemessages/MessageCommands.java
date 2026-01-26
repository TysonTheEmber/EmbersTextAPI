package net.tysontheember.emberstextapi.immersivemessages;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage.TextureSizingMode;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import org.slf4j.Logger;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID)
public class MessageCommands {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("emberstextapi")
                .then(testSubcommand())
                .then(sendSubcommand())
                .then(customSubcommand())
        );
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> testSubcommand() {
        return Commands.literal("test")
            .then(Commands.argument("id", IntegerArgumentType.integer(1, 9))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    runTest(player, id);
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> sendSubcommand() {
        return Commands.literal("send")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("duration", FloatArgumentType.floatArg())
                    .then(Commands.argument("fadeIn", IntegerArgumentType.integer(0))
                        .then(Commands.argument("fadeOut", IntegerArgumentType.integer(0))
                            .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> sendBasicMessage(ctx,
                                        IntegerArgumentType.getInteger(ctx, "fadeIn"),
                                        IntegerArgumentType.getInteger(ctx, "fadeOut")))))
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                            .executes(ctx -> sendBasicMessage(ctx,
                                    IntegerArgumentType.getInteger(ctx, "fadeIn"), 0))))
                    .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes(ctx -> sendBasicMessage(ctx, 0, 0)))));
    }

    private static int sendBasicMessage(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> ctx,
                                        int fadeIn, int fadeOut) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        float duration = FloatArgumentType.getFloat(ctx, "duration");
        String text = StringArgumentType.getString(ctx, "text");
        EmbersTextAPI.sendMessage(target,
                ImmersiveMessage.builder(duration, text)
                        .fadeInTicks(fadeIn)
                        .fadeOutTicks(fadeOut));
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> customSubcommand() {
        return Commands.literal("sendcustom")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("data", CompoundTagArgument.compoundTag())
                    .then(Commands.argument("duration", FloatArgumentType.floatArg())
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                CompoundTag tag = CompoundTagArgument.getCompoundTag(ctx, "data");
                                float duration = FloatArgumentType.getFloat(ctx, "duration");
                                String text = StringArgumentType.getString(ctx, "text");

                                // Build a case-insensitive lookup for tag keys
                                java.util.Map<String, String> keys = new java.util.HashMap<>();
                                for (String k : tag.getAllKeys()) {
                                    keys.put(k.toLowerCase(java.util.Locale.ROOT), k);
                                }

                                MutableComponent component;
                                try {
                                    component = Component.Serializer.fromJson(text);
                                    if (component == null) component = Component.literal(text);
                                } catch (JsonSyntaxException ignore) {
                                    component = Component.literal(text);
                                }
                                if (tag.contains("font")) {
                                    ResourceLocation font = ResourceLocation.tryParse(tag.getString("font"));
                                    if (font != null) {
                                        component = component.withStyle(style -> style.withFont(font));
                                    }
                                }
                                if (tag.contains("bold") && tag.getBoolean("bold")) {
                                    component = component.withStyle(style -> style.withBold(true));
                                }
                                if (tag.contains("italic") && tag.getBoolean("italic")) {
                                    component = component.withStyle(style -> style.withItalic(true));
                                }
                                if (tag.contains("underlined") && tag.getBoolean("underlined")) {
                                    component = component.withStyle(style -> style.withUnderlined(true));
                                }
                                if (tag.contains("strikethrough") && tag.getBoolean("strikethrough")) {
                                    component = component.withStyle(style -> style.withStrikethrough(true));
                                }
                                if (tag.contains("obfuscated") && tag.getBoolean("obfuscated")) {
                                    component = component.withStyle(style -> style.withObfuscated(true));
                                }
                                if (tag.contains("color")) {
                                    String colour = tag.getString("color");
                                    ChatFormatting fmt = ChatFormatting.getByName(colour);
                                    if (fmt != null) {
                                        component = component.withStyle(style -> style.withColor(fmt));
                                    } else {
                                        TextColor parsed = TextColor.parseColor(colour);
                                        if (parsed != null) {
                                            component = component.withStyle(style -> style.withColor(parsed));
                                        }
                                    }
                                }

                                ImmersiveMessage msg = new ImmersiveMessage(component, duration);
                                if (keys.containsKey("fadein")) {
                                    msg.fadeInTicks(tag.getInt(keys.get("fadein")));
                                }
                                if (keys.containsKey("fadeout")) {
                                    msg.fadeOutTicks(tag.getInt(keys.get("fadeout")));
                                }

                                // Text gradient (supports list of stops or {start,end})
                                if (tag.contains("gradient", Tag.TAG_LIST)) {
                                    ListTag list = tag.getList("gradient", Tag.TAG_STRING);
                                    java.util.List<TextColor> cols = new java.util.ArrayList<>();
                                    for (int i = 0; i < list.size(); i++) {
                                        TextColor c = parseColor(list.getString(i));
                                        if (c != null) cols.add(c);
                                    }
                                    if (cols.size() >= 2) {
                                        msg.gradient(cols);
                                    }
                                } else if (tag.contains("gradient", Tag.TAG_COMPOUND)) {
                                    CompoundTag grad = tag.getCompound("gradient");
                                    String startStr = grad.getString("start");
                                    String endStr = grad.getString("end");
                                    TextColor start = parseColor(startStr);
                                    TextColor end = parseColor(endStr);
                                    if (start != null && end != null) {
                                        msg.gradient(start, end);
                                    }
                                }

                                // Background gradient (maps to background's top/bottom colors)
                                if (tag.contains("bgGradient", Tag.TAG_LIST)) {
                                    ListTag list = tag.getList("bgGradient", Tag.TAG_STRING);
                                    java.util.List<ImmersiveColor> cols = new java.util.ArrayList<>();
                                    for (int i = 0; i < list.size(); i++) {
                                        ImmersiveColor c = parseImmersiveColor(list.getString(i));
                                        if (c != null) cols.add(c);
                                    }
                                    if (cols.size() >= 2) {
                                        msg.background(true);
                                        // Use first as top, last as bottom
                                        msg.backgroundGradient(cols.get(0), cols.get(cols.size() - 1));
                                    }
                                } else if (tag.contains("bgGradient", Tag.TAG_COMPOUND)) {
                                    CompoundTag grad = tag.getCompound("bgGradient");
                                    ImmersiveColor start = parseImmersiveColor(grad.getString("start"));
                                    ImmersiveColor end = parseImmersiveColor(grad.getString("end"));
                                    if (start != null && end != null) {
                                        msg.background(true);
                                        msg.backgroundGradient(start, end);
                                    }
                                }

                                // Border gradient (separate from background)
                                if (tag.contains("borderGradient", Tag.TAG_LIST)) {
                                    ListTag list = tag.getList("borderGradient", Tag.TAG_STRING);
                                    java.util.List<ImmersiveColor> cols = new java.util.ArrayList<>();
                                    for (int i = 0; i < list.size(); i++) {
                                        ImmersiveColor c = parseImmersiveColor(list.getString(i));
                                        if (c != null) cols.add(c);
                                    }
                                    if (cols.size() >= 2) {
                                        msg.background(true);
                                        // Use first as top, last as bottom
                                        msg.borderGradient(cols.get(0), cols.get(cols.size() - 1));
                                    }
                                } else if (tag.contains("borderGradient", Tag.TAG_COMPOUND)) {
                                    CompoundTag grad = tag.getCompound("borderGradient");
                                    ImmersiveColor start = parseImmersiveColor(grad.getString("start"));
                                    ImmersiveColor end = parseImmersiveColor(grad.getString("end"));
                                    if (start != null && end != null) {
                                        msg.background(true);
                                        msg.borderGradient(start, end);
                                    }
                                }

                                if (tag.contains("bgColor")) {
                                    msg.bgColor(tag.getString("bgColor"));
                                }

                                if (tag.contains("textureBackground", Tag.TAG_STRING)) {
                                    ResourceLocation texture = ResourceLocation.tryParse(tag.getString("textureBackground"));
                                    if (texture != null) {
                                        msg.textureBackground(texture);
                                    }
                                } else if (tag.contains("textureBackground", Tag.TAG_COMPOUND)) {
                                    CompoundTag tex = tag.getCompound("textureBackground");
                                    String key = tex.contains("location") ? "location" : tex.contains("texture") ? "texture" : null;
                                    ResourceLocation texture = key != null ? ResourceLocation.tryParse(tex.getString(key)) : null;
                                    if (texture != null) {
                                        int u = tex.contains("u") ? tex.getInt("u") : 0;
                                        int v = tex.contains("v") ? tex.getInt("v") : 0;
                                        int regionWidth = tex.contains("width") ? tex.getInt("width") : 256;
                                        int regionHeight = tex.contains("height") ? tex.getInt("height") : 256;
                                        int atlasWidth = tex.contains("atlasWidth") ? tex.getInt("atlasWidth") : regionWidth;
                                        int atlasHeight = tex.contains("atlasHeight") ? tex.getInt("atlasHeight") : regionHeight;
                                        msg.textureBackground(texture, u, v, regionWidth, regionHeight, atlasWidth, atlasHeight);

                                        // --- NEWER extended options block (kept) ---
                                        if (tex.contains("padding")) {
                                            msg.textureBackgroundPadding(tex.getFloat("padding"));
                                        }
                                        if (tex.contains("paddingX") || tex.contains("paddingY")) {
                                            float padX = tex.contains("paddingX") ? tex.getFloat("paddingX") : Float.NaN;
                                            float padY = tex.contains("paddingY") ? tex.getFloat("paddingY") : Float.NaN;
                                            msg.textureBackgroundPadding(padX, padY);
                                        }
                                        if (tex.contains("scale")) {
                                            msg.textureBackgroundScale(tex.getFloat("scale"));
                                        }
                                        if (tex.contains("scaleX") || tex.contains("scaleY")) {
                                            float scaleX = tex.contains("scaleX") ? tex.getFloat("scaleX") : Float.NaN;
                                            float scaleY = tex.contains("scaleY") ? tex.getFloat("scaleY") : Float.NaN;
                                            msg.textureBackgroundScale(scaleX, scaleY);
                                        }
                                        if (tex.contains("size")) {
                                            float size = tex.getFloat("size");
                                            msg.textureBackgroundSize(size, size);
                                        }
                                        if (tex.contains("sizeX")) {
                                            msg.textureBackgroundWidth(tex.getFloat("sizeX"));
                                        }
                                        if (tex.contains("sizeY")) {
                                            msg.textureBackgroundHeight(tex.getFloat("sizeY"));
                                        }
                                        if (tex.contains("drawWidth")) {
                                            msg.textureBackgroundWidth(tex.getFloat("drawWidth"));
                                        }
                                        if (tex.contains("drawHeight")) {
                                            msg.textureBackgroundHeight(tex.getFloat("drawHeight"));
                                        }
                                        if (tex.contains("x")) {
                                            msg.textureBackgroundWidth(tex.getFloat("x"));
                                        }
                                        if (tex.contains("y")) {
                                            msg.textureBackgroundHeight(tex.getFloat("y"));
                                        }
                                        if (tex.contains("resize")) {
                                            msg.textureBackgroundMode(tex.getBoolean("resize") ? TextureSizingMode.STRETCH : TextureSizingMode.CROP);
                                        }
                                        if (tex.contains("cut") && tex.getBoolean("cut")) {
                                            msg.textureBackgroundMode(TextureSizingMode.CROP);
                                        }
                                        if (tex.contains("mode")) {
                                            msg.textureBackgroundMode(TextureSizingMode.fromString(tex.getString("mode")));
                                        }
                                        // --- end newer block ---
                                    }
                                }

                                if (tag.contains("borderColor")) {
                                    ImmersiveColor border = parseImmersiveColor(tag.getString("borderColor"));
                                    if (border != null) {
                                        msg.background(true);
                                        msg.borderGradient(border, border);
                                    }
                                }

                                if (tag.contains("size")) {
                                    msg.scale(tag.getFloat("size"));
                                }

                                if (tag.contains("typewriter")) {
                                    float speed = tag.getFloat("typewriter");
                                    boolean center = tag.getBoolean("center");
                                    msg.typewriter(speed, center);
                                }
                                if (tag.contains("background") && tag.getBoolean("background")) {
                                    msg.background(true);
                                }
                                if (tag.contains("bgAlpha")) {
                                    msg.bgAlpha(tag.getFloat("bgAlpha"));
                                }
                                if (tag.contains("wrap")) {
                                    msg.wrap(tag.getInt("wrap"));
                                }
                                if (tag.contains("obfuscate")) {
                                    ObfuscateMode mode = ObfuscateMode.valueOf(tag.getString("obfuscate").toUpperCase());
                                    float speed = tag.contains("obfuscateSpeed") ? tag.getFloat("obfuscateSpeed") : 1f;
                                    msg.obfuscate(mode, speed);
                                }
                                if (tag.contains("anchor")) {
                                    msg.anchor(TextAnchor.valueOf(tag.getString("anchor").toUpperCase()));
                                }
                                if (tag.contains("align")) {
                                    msg.align(TextAnchor.valueOf(tag.getString("align").toUpperCase()));
                                }
                                if (tag.contains("offsetX") || tag.contains("offsetY")) {
                                    float x = tag.contains("offsetX") ? tag.getFloat("offsetX") : 0f;
                                    float y = tag.contains("offsetY") ? tag.getFloat("offsetY") : 0f;
                                    msg.offset(x, y);
                                }
                                if (tag.contains("shadow")) {
                                    msg.shadow(tag.getBoolean("shadow"));
                                }

                                if (keys.containsKey("shakewave")) {
                                    msg.shake(ShakeType.WAVE, tag.getFloat(keys.get("shakewave")));
                                } else if (keys.containsKey("wave")) {
                                    LOGGER.warn("Tag 'wave' is deprecated, use 'shakeWave' instead");
                                    msg.shake(ShakeType.WAVE, tag.getFloat(keys.get("wave")));
                                } else if (keys.containsKey("shakecircle")) {
                                    msg.shake(ShakeType.CIRCLE, tag.getFloat(keys.get("shakecircle")));
                                } else if (keys.containsKey("circle")) {
                                    LOGGER.warn("Tag 'circle' is deprecated, use 'shakeCircle' instead");
                                    msg.shake(ShakeType.CIRCLE, tag.getFloat(keys.get("circle")));
                                } else if (keys.containsKey("shakerandom")) {
                                    msg.shake(ShakeType.RANDOM, tag.getFloat(keys.get("shakerandom")));
                                } else if (keys.containsKey("random")) {
                                    LOGGER.warn("Tag 'random' is deprecated, use 'shakeRandom' instead");
                                    msg.shake(ShakeType.RANDOM, tag.getFloat(keys.get("random")));
                                }

                                if (keys.containsKey("charshakewave")) {
                                    msg.charShake(ShakeType.WAVE, tag.getFloat(keys.get("charshakewave")));
                                } else if (keys.containsKey("wavechar")) {
                                    LOGGER.warn("Tag 'waveChar' is deprecated, use 'charShakeWave' instead");
                                    msg.charShake(ShakeType.WAVE, tag.getFloat(keys.get("wavechar")));
                                } else if (keys.containsKey("charshakecircle")) {
                                    msg.charShake(ShakeType.CIRCLE, tag.getFloat(keys.get("charshakecircle")));
                                } else if (keys.containsKey("circlechar")) {
                                    LOGGER.warn("Tag 'circleChar' is deprecated, use 'charShakeCircle' instead");
                                    msg.charShake(ShakeType.CIRCLE, tag.getFloat(keys.get("circlechar")));
                                } else if (keys.containsKey("charshakerandom")) {
                                    msg.charShake(ShakeType.RANDOM, tag.getFloat(keys.get("charshakerandom")));
                                } else if (keys.containsKey("randomchar")) {
                                    LOGGER.warn("Tag 'randomChar' is deprecated, use 'charShakeRandom' instead");
                                    msg.charShake(ShakeType.RANDOM, tag.getFloat(keys.get("randomchar")));
                                }

                                EmbersTextAPI.sendMessage(target, msg);
                                return Command.SINGLE_SUCCESS;
                            })))));
    }

    private static void runTest(ServerPlayer player, int id) {
        switch (id) {
            case 1 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(75f, "Basic message"));
            case 2 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Typewriter demo").typewriter(1f));
            case 3 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(200f, "Secret text").obfuscate(ObfuscateMode.RANDOM, 0.1f));
            case 4 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Framed text").background(true));
            case 5 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Wrapped text demo that is quite long").wrap(120));
            case 6 -> {
                ResourceLocation font = new ResourceLocation(EmbersTextAPI.MODID, "norse");
                Component text = Component.literal("\u16A0\u16A2\u16A6\u16A8\u16AB\u16B2").withStyle(s -> s.withFont(font));
                EmbersTextAPI.sendMessage(player, new ImmersiveMessage(text, 100f));
            }
            case 7 -> {
                MutableComponent component = Component.literal("You shall die here...")
                        .withStyle(s -> s.withFont(new ResourceLocation(EmbersTextAPI.MODID, "norse")))
                        .withStyle(s -> s.withBold(true));
                ImmersiveMessage msg = new ImmersiveMessage(component, 250f)
                        .scale(2f)
                        .background(true)
                        .obfuscate(ObfuscateMode.LEFT, 0.1f)
                        .anchor(TextAnchor.CENTER_CENTER)
                        .charShake(ShakeType.RANDOM, 0.5f);
                EmbersTextAPI.sendMessage(player, msg);
            }
            case 8 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Jittery chars").charShake(ShakeType.RANDOM, 0.5f));
            case 9 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Wavy jitter")
                            .shake(ShakeType.WAVE, 0.5f)
                            .charShake(ShakeType.RANDOM, 2f));
        }
    }

    private static TextColor parseColor(String value) {
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) {
            return TextColor.fromLegacyFormat(fmt);
        }
        return TextColor.parseColor(value);
    }

    private static ImmersiveColor parseImmersiveColor(String value) {
        if (value == null) return null;

        String v = value.trim();
        try {
            if (v.startsWith("#")) v = v.substring(1);
            if (v.startsWith("0x")) v = v.substring(2);
            if (v.length() == 8) {
                return new ImmersiveColor((int) Long.parseLong(v, 16));
            } else if (v.length() == 6) {
                return new ImmersiveColor(0xFF000000 | Integer.parseInt(v, 16));
            }
        } catch (NumberFormatException ignored) {
        }

        TextColor c = parseColor(value);
        return c != null ? new ImmersiveColor(0xFF000000 | c.getValue()) : null;
    }
}
