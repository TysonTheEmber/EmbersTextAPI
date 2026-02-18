package net.tysontheember.emberstextapi.commands;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
import net.tysontheember.emberstextapi.fabric.EmbersTextAPIFabric;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage.TextureSizingMode;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAlign;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import net.tysontheember.emberstextapi.platform.NetworkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Message commands for Fabric.
 * Ported from Forge MessageCommands.
 */
public class FabricMessageCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/Commands");

    public static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> testSubcommand() {
        return Commands.literal("test")
            .then(Commands.argument("id", IntegerArgumentType.integer(1, 33))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    runTest(player, id);
                    return Command.SINGLE_SUCCESS;
                }))
            .then(FabricEffectTestCommands.buildEffectTestCommand())
            .then(FabricEffectTestCommands.buildComboCommand());
    }

    public static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> sendSubcommand() {
        return Commands.literal("send")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("duration", FloatArgumentType.floatArg())
                    .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes(ctx -> sendBasicMessage(ctx)))));
    }

    private static int sendBasicMessage(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        float duration = FloatArgumentType.getFloat(ctx, "duration");
        String text = StringArgumentType.getString(ctx, "text");

        ImmersiveMessage msg;
        if (text.contains("<") && text.contains(">")) {
            msg = ImmersiveMessage.fromMarkup(duration, text);
        } else {
            msg = ImmersiveMessage.builder(duration, text);
        }

        NetworkHelper.getInstance().sendMessage(target, msg);
        return Command.SINGLE_SUCCESS;
    }

    public static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> queueSubcommand() {
        return Commands.literal("queue")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("channel", StringArgumentType.word())
                    .then(Commands.argument("queue_definition", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            String channel = StringArgumentType.getString(ctx, "channel");
                            String rawDef = StringArgumentType.getString(ctx, "queue_definition");

                            String[] rawSteps = rawDef.split(" \\| ");
                            List<List<ImmersiveMessage>> steps = new ArrayList<>();

                            for (String rawStep : rawSteps) {
                                String[] rawMessages = rawStep.split(" & ");
                                List<ImmersiveMessage> msgs = new ArrayList<>();

                                for (String rawMsg : rawMessages) {
                                    String text = rawMsg.trim();
                                    if (text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
                                        text = text.substring(1, text.length() - 1);
                                    }

                                    Object[] extracted = MarkupParser.extractDuration(text);
                                    float dur = (float) extracted[0];
                                    String markup = (String) extracted[1];

                                    if (dur < 0) {
                                        LOGGER.warn("No <dur:N> tag in queue message, defaulting to 60 ticks: {}", markup);
                                        dur = 60f;
                                    }

                                    msgs.add(ImmersiveMessage.fromMarkup(dur, markup));
                                }
                                steps.add(msgs);
                            }

                            NetworkHelper.getInstance().sendQueue(target, channel, steps);
                            return Command.SINGLE_SUCCESS;
                        }))));
    }

    public static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> clearQueueSubcommand() {
        return Commands.literal("clearqueue")
            .then(Commands.argument("player", EntityArgument.player())
                .executes(ctx -> {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                    NetworkHelper.getInstance().sendClearAllQueues(target);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("channel", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        String channel = StringArgumentType.getString(ctx, "channel");
                        NetworkHelper.getInstance().sendClearQueue(target, channel);
                        return Command.SINGLE_SUCCESS;
                    })));
    }

    private static void runTest(ServerPlayer player, int id) {
        NetworkHelper net = NetworkHelper.getInstance();
        switch (id) {
            case 1 -> net.sendMessage(player,
                    ImmersiveMessage.builder(75f, "Basic message"));
            case 2 -> net.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Typewriter demo").typewriter(1f));
            case 3 -> net.sendMessage(player,
                    ImmersiveMessage.builder(200f, "Secret text").obfuscate(ObfuscateMode.RANDOM, 0.1f));
            case 4 -> net.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Framed text").background(true));
            case 5 -> net.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Wrapped text demo that is quite long").wrap(120));
            case 6 -> {
                ResourceLocation font = new ResourceLocation(EmbersTextAPIFabric.MODID, "norse");
                Component text = Component.literal("\u16A0\u16A2\u16A6\u16A8\u16AB\u16B2").withStyle(s -> s.withFont(font));
                net.sendMessage(player, new ImmersiveMessage(text, 100f).scale(5));
            }
            case 7 -> {
                MutableComponent component = Component.literal("You shall die here...")
                        .withStyle(s -> s.withFont(new ResourceLocation(EmbersTextAPIFabric.MODID, "norse")))
                        .withStyle(s -> s.withBold(true));
                ImmersiveMessage msg = new ImmersiveMessage(component, 250f)
                        .scale(2f)
                        .background(true)
                        .obfuscate(ObfuscateMode.LEFT, 0.1f)
                        .anchor(TextAnchor.MIDDLE)
                        .charShake(ShakeType.RANDOM, 0.5f);
                net.sendMessage(player, msg);
            }
            case 8 -> net.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Jittery chars").charShake(ShakeType.RANDOM, 0.5f));
            case 9 -> net.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Wavy jitter")
                            .shake(ShakeType.WAVE, 0.5f)
                            .charShake(ShakeType.RANDOM, 2f));
            case 10 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<bold>Bold</bold> and <italic>italic</italic> text"));
            case 11 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(150f, "<grad from=#ff0000 to=#00ff00>Gradient</grad> <typewriter speed=2.0>text!</typewriter>"));
            case 12 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(200f, "<shake><grad from=#ff0000 to=#0000ff>Nested effects!</grad></shake>"));
            case 13 -> {
                CompoundTag data = new CompoundTag();
                data.putBoolean("background", true);
                data.putString("bgColor", "#AA004400");
                data.putString("borderColor", "gold");
                ImmersiveMessage msg = ImmersiveMessage.fromMarkup(150f, "<c value=white><bold>Framed markup text</bold></c> with <italic>styling</italic>");
                applyNbtToSpanMessage(msg, data, createKeysMap(data));
                net.sendMessage(player, msg);
            }
            case 14 -> {
                CompoundTag data = new CompoundTag();
                data.putString("font", "emberstextapi:norse");
                data.putBoolean("background", true);
                ImmersiveMessage msg = ImmersiveMessage.fromMarkup(120f, "<grad from=gold to=red><bold>Norse font with gradient!</bold></grad>");
                applyNbtToSpanMessage(msg, data, createKeysMap(data));
                net.sendMessage(player, msg);
            }
            case 15 -> {
                CompoundTag data = new CompoundTag();
                data.putString("font0", "emberstextapi:norse");
                data.putString("font1", "minecraft:alt");
                data.putBoolean("background", true);
                data.putString("bgColor", "#33000000");
                ImmersiveMessage msg = ImmersiveMessage.fromMarkup(180f, "<bold>Norse</bold> <italic>Alt</italic> <c value=gold>Default</c>");
                applyNbtToSpanMessage(msg, data, createKeysMap(data));
                net.sendMessage(player, msg);
            }
            case 16 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(150f, "You found <item value=\"minecraft:dirt\" size=1></item> x5 and <item value=\"minecraft:diamond\" size=1></item>!"));
            case 17 -> {
                List<TextSpan> spans = new java.util.ArrayList<>();
                spans.add(new TextSpan("You got "));
                spans.add(new TextSpan("").item("minecraft:diamond", 3));
                spans.add(new TextSpan(" diamonds!"));
                ImmersiveMessage msg = new ImmersiveMessage(spans, 100f);
                net.sendMessage(player, msg);
            }
            case 18 -> {
                String markup = "Test <item value=\"minecraft:gold_ingot\"></item> item";
                LOGGER.info("Sending markup: {}", markup);
                ImmersiveMessage msg = ImmersiveMessage.fromMarkup(100f, markup);
                LOGGER.info("Parsed {} spans", msg.getSpans().size());
                for (int idx = 0; idx < msg.getSpans().size(); idx++) {
                    TextSpan s = msg.getSpans().get(idx);
                    LOGGER.info("Span {}: content='{}', itemId='{}', itemCount={}",
                        idx, s.getContent(), s.getItemId(), s.getItemCount());
                }
                net.sendMessage(player, msg);
            }
            case 19 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(150f, "Item with offset: <item value=\"minecraft:emerald\" offsetX=2 offsetY=-3></item> test"));
            case 20 -> {
                List<TextSpan> spans = new java.util.ArrayList<>();
                spans.add(new TextSpan("Offset item: "));
                spans.add(new TextSpan("").item("minecraft:redstone", 1).itemOffset(0, -2));
                spans.add(new TextSpan(" raised"));
                ImmersiveMessage msg = new ImmersiveMessage(spans, 100f);
                net.sendMessage(player, msg);
            }
            case 21 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(150f, "Beware of <entity value=\"minecraft:creeper\"></entity> ahead!").scale(4));
            case 22 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(150f, "Small <entity value=\"minecraft:zombie\" scale=0.5></entity> mob").scale(4));
            case 23 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(150f, "Side view <entity value=\"minecraft:skeleton\" yaw=90 pitch=0></entity> mob"));
            case 24 -> {
                List<TextSpan> spans = new java.util.ArrayList<>();
                spans.add(new TextSpan("You see a "));
                spans.add(new TextSpan("").entity("minecraft:pig", 0.7f));
                spans.add(new TextSpan(" nearby"));
                ImmersiveMessage msg = new ImmersiveMessage(spans, 100f).scale(4);
                net.sendMessage(player, msg);
            }
            case 25 -> {
                List<TextSpan> spans = new java.util.ArrayList<>();
                spans.add(new TextSpan("Rotated "));
                spans.add(new TextSpan("").entity("minecraft:cow", 0.7f).entityRotation(180, 20));
                spans.add(new TextSpan(" facing away"));
                ImmersiveMessage msg = new ImmersiveMessage(spans, 100f);
                net.sendMessage(player, msg);
            }
            case 26 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<rainbow>Rainbow Test</rainbow>"));
            case 27 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<glitch>Glitch Test</glitch>"));
            case 28 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<bounce>Bounce Test</bounce>"));
            case 29 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<pulse>Pulse Test</pulse>"));
            case 30 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<swing>Swing Test</swing>"));
            case 31 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<turb>Turbulence Test</turb>"));
            case 32 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<wave>Wave Test</wave>"));
            case 33 -> net.sendMessage(player,
                    ImmersiveMessage.fromMarkup(150f, "<rainbow><wave>Combined Effects Test</wave></rainbow>"));
        }
    }

    static java.util.Map<String, String> createKeysMap(CompoundTag tag) {
        java.util.Map<String, String> keys = new java.util.HashMap<>();
        for (String k : tag.getAllKeys()) {
            keys.put(k.toLowerCase(java.util.Locale.ROOT), k);
        }
        return keys;
    }

    static TextColor parseColor(String value) {
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) {
            return TextColor.fromLegacyFormat(fmt);
        }
        return TextColor.parseColor(value);
    }

    static ImmersiveColor parseImmersiveColor(String value) {
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

    static void applyNbtToSpanMessage(ImmersiveMessage msg, CompoundTag tag, java.util.Map<String, String> keys) {
        if (tag.contains("background") && tag.getBoolean("background")) {
            msg.background(true);
        }
        if (tag.contains("bgColor")) {
            msg.bgColor(tag.getString("bgColor"));
        }
        if (tag.contains("bgAlpha")) {
            msg.bgAlpha(tag.getFloat("bgAlpha"));
        }

        if (tag.contains("bgGradient", Tag.TAG_LIST)) {
            ListTag list = tag.getList("bgGradient", Tag.TAG_STRING);
            java.util.List<ImmersiveColor> cols = new java.util.ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                ImmersiveColor c = parseImmersiveColor(list.getString(i));
                if (c != null) cols.add(c);
            }
            if (cols.size() >= 2) {
                msg.background(true);
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

        if (tag.contains("borderGradient", Tag.TAG_LIST)) {
            ListTag list = tag.getList("borderGradient", Tag.TAG_STRING);
            java.util.List<ImmersiveColor> cols = new java.util.ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                ImmersiveColor c = parseImmersiveColor(list.getString(i));
                if (c != null) cols.add(c);
            }
            if (cols.size() >= 2) {
                msg.background(true);
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
        if (tag.contains("wrap")) {
            msg.wrap(tag.getInt("wrap"));
        }
        if (tag.contains("anchor")) {
            msg.anchor(TextAnchor.valueOf(tag.getString("anchor").toUpperCase()));
        }
        if (tag.contains("align")) {
            msg.align(TextAlign.valueOf(tag.getString("align").toUpperCase()));
        }
        if (tag.contains("offsetX") || tag.contains("offsetY")) {
            float x = tag.contains("offsetX") ? tag.getFloat("offsetX") : 0f;
            float y = tag.contains("offsetY") ? tag.getFloat("offsetY") : 0f;
            msg.offset(x, y);
        }
        if (tag.contains("shadow")) {
            msg.shadow(tag.getBoolean("shadow"));
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
            }
        }

        if (tag.contains("font")) {
            ResourceLocation font = ResourceLocation.tryParse(tag.getString("font"));
            if (font != null && msg.isSpanMode()) {
                java.util.List<TextSpan> spans = msg.getSpans();
                for (TextSpan span : spans) {
                    span.font(font);
                }
            }
        }

        if (msg.isSpanMode()) {
            java.util.List<TextSpan> spans = msg.getSpans();
            for (int i = 0; i < spans.size(); i++) {
                String fontKey = "font" + i;
                if (tag.contains(fontKey)) {
                    ResourceLocation font = ResourceLocation.tryParse(tag.getString(fontKey));
                    if (font != null) {
                        spans.get(i).font(font);
                    }
                }
            }
        }
    }
}
