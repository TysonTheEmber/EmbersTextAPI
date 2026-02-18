package net.tysontheember.emberstextapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.tysontheember.emberstextapi.platform.NetworkHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Effect test commands for Fabric 1.21.1.
 * Ported from NeoForge EffectTestCommands with Fabric platform abstractions.
 */
public class FabricEffectTestCommands {

    private static final List<String> EFFECT_NAMES = List.of(
            "rainbow", "glitch", "wave", "bounce", "shake", "pulse",
            "swing", "turb", "circle", "wiggle", "pend", "scroll",
            "grad", "fade", "shadow", "neon", "type", "font", "obfuscate",
            "all"
    );

    private static final List<String> MODES = List.of("immersive", "chat", "item");

    private static final List<EffectDef> INDIVIDUAL_EFFECTS = List.of(
            new EffectDef("rainbow", "<rainbow>Rainbow - Color cycling</rainbow>", Items.DIAMOND),
            new EffectDef("glitch", "<glitch>Glitch - Digital distortion</glitch>", Items.ENDER_PEARL),
            new EffectDef("wave", "<wave>Wave - Vertical motion</wave>", Items.TRIDENT),
            new EffectDef("bounce", "<bounce>Bounce - Bouncing animation</bounce>", Items.SLIME_BALL),
            new EffectDef("shake", "<shake>Shake - Jittery vibration</shake>", Items.TNT),
            new EffectDef("pulse", "<pulse>Pulse - Fading opacity</pulse>", Items.REDSTONE),
            new EffectDef("swing", "<swing>Swing - Pendulum rotation</swing>", Items.CLOCK),
            new EffectDef("turb", "<turb>Turbulence - Chaotic motion</turb>", Items.FIREWORK_ROCKET),
            new EffectDef("circle", "<circle>Circle - Orbital motion</circle>", Items.ENDER_EYE),
            new EffectDef("wiggle", "<wiggle>Wiggle - Quick rotation</wiggle>", Items.FEATHER),
            new EffectDef("pend", "<pend>Pendulum - Swaying motion</pend>", Items.CHAIN),
            new EffectDef("scroll", "<scroll>Scroll - Directional wave</scroll>", Items.MAP),
            new EffectDef("grad", "<grad from=FF0000 to=00FF00>Gradient - Color blend</grad>", Items.LEATHER),
            new EffectDef("fade", "<fade>Fade - Opacity cycle</fade>", Items.GLASS),
            new EffectDef("shadow", "<shadow c=FF0000 x=2 y=2>Shadow - Drop shadow</shadow>", Items.INK_SAC),
            new EffectDef("neon", "<neon c=00FFFF>Neon - Glow effect</neon>", Items.GLOWSTONE),
            new EffectDef("type", "<type s=1.5>Typewriter - Reveal effect</type>", Items.WRITABLE_BOOK),
            new EffectDef("font", "<font id=emberstextapi:metamorphous>Metamorphous Fantasy Font!</font>", Items.ENCHANTED_BOOK),
            new EffectDef("obfuscate", "<obfuscate mode=reveal speed=80>Revealing obfuscation</obfuscate>", Items.ENDER_EYE)
    );

    private static final List<EffectDef> COMBO_EFFECTS = List.of(
            new EffectDef("rainbow+wave", "<rainbow><wave>Rainbow + Wave</wave></rainbow>", Items.PRISMARINE_SHARD),
            new EffectDef("rainbow+bounce", "<rainbow><bounce>Rainbow + Bounce</bounce></rainbow>", Items.RABBIT_FOOT),
            new EffectDef("glitch+shake", "<glitch><shake>Glitch + Shake</shake></glitch>", Items.END_CRYSTAL),
            new EffectDef("neon+pulse", "<neon c=FF00FF><pulse>Neon + Pulse</pulse></neon>", Items.SEA_LANTERN),
            new EffectDef("rainbow+neon", "<rainbow><neon>Rainbow + Neon</neon></rainbow>", Items.BEACON),
            new EffectDef("wave+bounce", "<wave><bounce>Wave + Bounce</bounce></wave>", Items.HONEY_BOTTLE),
            new EffectDef("grad+wave", "<grad from=FF0000 to=0000FF><wave>Gradient + Wave</wave></grad>", Items.AMETHYST_SHARD),
            new EffectDef("rainbow+wave+neon", "<rainbow><wave><neon>Rainbow + Wave + Neon</neon></wave></rainbow>", Items.NETHER_STAR),
            new EffectDef("glitch+rainbow+shake", "<glitch><rainbow><shake>Glitch + Rainbow + Shake</shake></rainbow></glitch>", Items.DRAGON_EGG),
            new EffectDef("obfuscate+rainbow", "<obfuscate mode=random><rainbow>Random Obfuscate + Rainbow</rainbow></obfuscate>", Items.CHORUS_FRUIT),
            new EffectDef("obfuscate-reveal", "<obfuscate mode=reveal speed=100 direction=center>Revealing from center...</obfuscate>", Items.SPYGLASS)
    );

    private record EffectDef(String name, String markup, Item item) {}

    private static final int EFFECT_DURATION_TICKS = 160;

    public static ArgumentBuilder<CommandSourceStack, ?> buildEffectTestCommand() {
        return Commands.literal("effect")
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(FabricEffectTestCommands::suggestEffects)
                        .executes(ctx -> runEffect(ctx, null))
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(FabricEffectTestCommands::suggestModes)
                                .executes(ctx -> runEffect(ctx, StringArgumentType.getString(ctx, "mode")))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> buildComboCommand() {
        return Commands.literal("combo")
                .then(Commands.argument("effects", StringArgumentType.greedyString())
                        .suggests(FabricEffectTestCommands::suggestEffects)
                        .executes(FabricEffectTestCommands::runCombo));
    }

    private static CompletableFuture<Suggestions> suggestEffects(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String name : EFFECT_NAMES) {
            if (name.startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestModes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String mode : MODES) {
            if (mode.startsWith(remaining)) {
                builder.suggest(mode);
            }
        }
        return builder.buildFuture();
    }

    private static int runEffect(CommandContext<CommandSourceStack> ctx, String mode) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String effectName = StringArgumentType.getString(ctx, "name").toLowerCase();

        if (effectName.equals("all")) {
            if (mode == null) {
                runAllChat(player);
                runAllItems(player);
                runAllImmersive(player);
                player.sendSystemMessage(Component.literal("Starting full effect showcase..."));
            } else {
                switch (mode.toLowerCase()) {
                    case "chat" -> runAllChat(player);
                    case "item" -> runAllItems(player);
                    case "immersive" -> runAllImmersive(player);
                    default -> {
                        player.sendSystemMessage(Component.literal("Unknown mode: " + mode));
                        return 0;
                    }
                }
            }
            return Command.SINGLE_SUCCESS;
        }

        if (effectName.equals("obfuscate") || effectName.equals("obf")) {
            String actualMode = (mode == null) ? "immersive" : mode.toLowerCase();
            switch (actualMode) {
                case "chat" -> {
                    player.sendSystemMessage(Component.literal("<obfuscate>Constant obfuscation</obfuscate>"));
                    player.sendSystemMessage(Component.literal("<obfuscate mode=reveal speed=80>Revealing from left...</obfuscate>"));
                    player.sendSystemMessage(Component.literal("<obfuscate mode=hide speed=80>Hiding from left...</obfuscate>"));
                    player.sendSystemMessage(Component.literal("<obfuscate mode=random>Random flickering</obfuscate>"));
                }
                case "item" -> {
                    giveItem(player, "obf-constant", "<obfuscate>Constant</obfuscate>");
                    giveItem(player, "obf-reveal", "<obfuscate mode=reveal speed=80>Revealing</obfuscate>");
                    giveItem(player, "obf-hide", "<obfuscate mode=hide speed=80>Hiding</obfuscate>");
                    giveItem(player, "obf-random", "<obfuscate mode=random>Random</obfuscate>");
                    player.sendSystemMessage(Component.literal("Gave you 4 obfuscate items!"));
                }
                default -> {
                    var server = player.getServer();
                    if (server == null) return 0;

                    NetworkHelper.getInstance().sendClearAllQueues(player);

                    String[] modes = {
                        "<obfuscate>Constant obfuscation</obfuscate>",
                        "<obfuscate mode=reveal speed=80 direction=left>Revealing left to right</obfuscate>",
                        "<obfuscate mode=hide speed=80 direction=right>Hiding right to left</obfuscate>",
                        "<obfuscate mode=random>Random flickering mask</obfuscate>"
                    };

                    int baseTick = server.getTickCount();
                    for (int i = 0; i < modes.length; i++) {
                        String markup = modes[i];
                        int delayTicks = i * 100;

                        server.tell(new net.minecraft.server.TickTask(baseTick + delayTicks, () -> {
                            if (player.isAlive() && player.connection != null) {
                                NetworkHelper.getInstance().sendClearAllQueues(player);

                                var msg = net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage
                                        .fromMarkup(90f, markup)
                                        .anchor(net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor.MIDDLE)
                                        .scale(2.5f)
                                        .fadeInTicks(10)
                                        .fadeOutTicks(10)
                                        .background(true);

                                NetworkHelper.getInstance().sendMessage(player, msg);
                            }
                        }));
                    }

                    player.sendSystemMessage(Component.literal("Playing 4 obfuscate modes..."));
                }
            }
            return Command.SINGLE_SUCCESS;
        }

        String markup = buildMarkup(effectName);
        if (markup == null) {
            player.sendSystemMessage(Component.literal("Unknown effect: " + effectName));
            return 0;
        }

        String actualMode = (mode == null) ? "immersive" : mode.toLowerCase();
        switch (actualMode) {
            case "chat" -> player.sendSystemMessage(Component.literal(markup));
            case "item" -> giveItem(player, effectName, markup);
            default -> sendImmersive(player, markup);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int runCombo(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String input = StringArgumentType.getString(ctx, "effects");
        String[] effects = input.split("\\s+");

        StringBuilder markup = new StringBuilder();
        StringBuilder closing = new StringBuilder();

        for (String effect : effects) {
            String tag = getTagForEffect(effect.toLowerCase());
            if (tag != null) {
                markup.append("<").append(tag).append(">");
                closing.insert(0, "</" + tag.split("\\s")[0] + ">");
            }
        }

        markup.append("Combined Effects!");
        markup.append(closing);

        sendImmersive(player, markup.toString());
        return Command.SINGLE_SUCCESS;
    }

    // ==================== ALL CHAT ====================

    private static void runAllChat(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("=== Individual Effects ==="));
        for (EffectDef effect : INDIVIDUAL_EFFECTS) {
            player.sendSystemMessage(Component.literal(effect.markup));
        }

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("=== Combo Effects ==="));
        for (EffectDef effect : COMBO_EFFECTS) {
            player.sendSystemMessage(Component.literal(effect.markup));
        }
    }

    // ==================== ALL ITEMS ====================

    private static void runAllItems(ServerPlayer player) {
        int count = 0;

        for (EffectDef effect : INDIVIDUAL_EFFECTS) {
            giveEffectItem(player, effect);
            count++;
        }

        for (EffectDef effect : COMBO_EFFECTS) {
            giveEffectItem(player, effect);
            count++;
        }

        player.sendSystemMessage(Component.literal("Gave you " + count + " effect items!"));
    }

    private static void giveEffectItem(ServerPlayer player, EffectDef effect) {
        ItemStack stack = new ItemStack(effect.item);
        // MC 1.21.1: Use DataComponents API
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(effect.markup));
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    // ==================== ALL IMMERSIVE ====================

    private static void runAllImmersive(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return;

        NetworkHelper.getInstance().sendClearAllQueues(player);

        java.util.List<EffectDef> allEffects = new java.util.ArrayList<>();
        allEffects.addAll(INDIVIDUAL_EFFECTS);
        allEffects.addAll(COMBO_EFFECTS);

        player.sendSystemMessage(Component.literal("Playing " + allEffects.size() + " effects (8 sec each)..."));

        int baseTick = server.getTickCount();

        for (int i = 0; i < allEffects.size(); i++) {
            EffectDef effect = allEffects.get(i);
            int delayTicks = i * EFFECT_DURATION_TICKS;

            server.tell(new net.minecraft.server.TickTask(baseTick + delayTicks, () -> {
                if (player.isAlive() && player.connection != null) {
                    NetworkHelper.getInstance().sendClearAllQueues(player);

                    var msg = net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage
                            .fromMarkup(EFFECT_DURATION_TICKS - 20f, effect.markup)
                            .anchor(net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor.MIDDLE)
                            .scale(2.5f)
                            .fadeInTicks(10)
                            .fadeOutTicks(10)
                            .background(true);

                    NetworkHelper.getInstance().sendMessage(player, msg);
                }
            }));
        }
    }

    // ==================== SINGLE EFFECT HELPERS ====================

    private static String buildMarkup(String effect) {
        return switch (effect) {
            case "rainbow" -> "<rainbow>Rainbow cycling colors</rainbow>";
            case "glitch" -> "<glitch>Glitchy distorted text</glitch>";
            case "wave" -> "<wave>Smooth wave motion</wave>";
            case "bounce" -> "<bounce>Bouncing animation</bounce>";
            case "shake" -> "<shake>Shaking jittery text</shake>";
            case "pulse" -> "<pulse>Pulsing opacity fade</pulse>";
            case "swing" -> "<swing>Swinging pendulum rotation</swing>";
            case "turb", "turbulence" -> "<turb>Turbulent chaotic motion</turb>";
            case "circle" -> "<circle>Circular orbit motion</circle>";
            case "wiggle" -> "<wiggle>Quick wiggle rotation</wiggle>";
            case "pend", "pendulum" -> "<pend>Pendulum swing effect</pend>";
            case "scroll" -> "<scroll>Scrolling wave motion</scroll>";
            case "grad", "gradient" -> "<grad from=FF0000 to=00FF00>Gradient color blend</grad>";
            case "fade" -> "<fade>Fading in and out</fade>";
            case "shadow" -> "<shadow c=FF0000 x=2 y=2>Text with shadow</shadow>";
            case "neon", "glow" -> "<neon c=00FFFF>Glowing neon effect</neon>";
            case "type", "typewriter" -> "<type s=1>Typewriter reveal...</type>";
            case "font" -> "<font id=emberstextapi:metamorphous><rainbow>Metamorphous with Rainbow!</rainbow></font>";
            case "obfuscate", "obf" -> null; // handled specially
            default -> null;
        };
    }

    private static String getTagForEffect(String effect) {
        return switch (effect) {
            case "rainbow" -> "rainbow";
            case "glitch" -> "glitch";
            case "wave" -> "wave";
            case "bounce" -> "bounce";
            case "shake" -> "shake";
            case "pulse" -> "pulse";
            case "swing" -> "swing";
            case "turb", "turbulence" -> "turb";
            case "circle" -> "circle";
            case "wiggle" -> "wiggle";
            case "pend", "pendulum" -> "pend";
            case "scroll" -> "scroll";
            case "grad", "gradient" -> "grad from=FF0000 to=0000FF";
            case "fade" -> "fade";
            case "shadow" -> "shadow";
            case "neon", "glow" -> "neon c=00FFFF";
            case "type", "typewriter" -> "type s=1";
            case "font" -> "font id=emberstextapi:metamorphous";
            case "obfuscate", "obf" -> "obfuscate mode=random";
            default -> null;
        };
    }

    private static void sendImmersive(ServerPlayer player, String markup) {
        NetworkHelper.getInstance().sendClearAllQueues(player);

        var msg = net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage
                .fromMarkup(100f, markup)
                .anchor(net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor.MIDDLE)
                .scale(2.5f)
                .fadeInTicks(10)
                .fadeOutTicks(10)
                .background(true);

        NetworkHelper.getInstance().sendMessage(player, msg);
    }

    private static void giveItem(ServerPlayer player, String effectName, String markup) {
        Item item = switch (effectName) {
            case "rainbow" -> Items.DIAMOND;
            case "glitch" -> Items.ENDER_PEARL;
            case "wave" -> Items.TRIDENT;
            case "bounce" -> Items.SLIME_BALL;
            case "shake" -> Items.TNT;
            case "pulse" -> Items.REDSTONE;
            case "neon", "glow" -> Items.GLOWSTONE;
            case "type", "typewriter" -> Items.WRITABLE_BOOK;
            case "font" -> Items.ENCHANTED_BOOK;
            case "obfuscate", "obf" -> Items.ENDER_EYE;
            default -> Items.PAPER;
        };

        ItemStack stack = new ItemStack(item);
        // MC 1.21.1: Use DataComponents API
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(markup));

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.sendSystemMessage(Component.literal("Gave you: " + effectName + " item"));
    }
}
