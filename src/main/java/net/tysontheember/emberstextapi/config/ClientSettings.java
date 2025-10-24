package net.tysontheember.emberstextapi.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Forge client configuration controlling global styled rendering behaviour.
 */
public final class ClientSettings {
    private static final String CATEGORY = "styledRendering";

    private static final ClientSettings INSTANCE;
    public static final ForgeConfigSpec SPEC;

    private final ForgeConfigSpec.BooleanValue enableStyledRendering;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistScreens;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistModIds;
    private final ForgeConfigSpec.IntValue maxEffectsPerGlyph;
    private final ForgeConfigSpec.IntValue maxSpanDepth;

    private volatile boolean styledRenderingEnabledValue = true;
    private volatile Set<String> screenBlacklistValue = Collections.emptySet();
    private volatile Set<String> modIdBlacklistValue = Collections.emptySet();
    private volatile int maxEffectsPerGlyphValue = 3;
    private volatile int maxSpanDepthValue = 8;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        INSTANCE = new ClientSettings(builder);
        SPEC = builder.build();
    }

    private ClientSettings(ForgeConfigSpec.Builder builder) {
        builder.push(CATEGORY);
        enableStyledRendering = builder
            .comment("Master toggle for markup-driven styled rendering")
            .define("enableStyledRendering", true);
        blacklistScreens = builder
            .comment("Fully qualified screen class names that should bypass styled rendering")
            .defineListAllowEmpty(
                List.of("blacklistScreens"),
                () -> List.of(),
                entry -> entry instanceof String
            );
        blacklistModIds = builder
            .comment("Mod identifiers that should bypass styled rendering")
            .defineListAllowEmpty(
                List.of("blacklistModIds"),
                () -> List.of(),
                entry -> entry instanceof String
            );
        maxEffectsPerGlyph = builder
            .comment("Maximum number of effect layers evaluated per glyph")
            .defineInRange("maxEffectsPerGlyph", 3, 0, 16);
        maxSpanDepth = builder
            .comment("Maximum nested span depth allowed before falling back to vanilla rendering")
            .defineInRange("maxSpanDepth", 8, 0, 64);
        builder.pop();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public static boolean isStyledRenderingEnabled() {
        return INSTANCE.styledRenderingEnabledValue;
    }

    public static void setStyledRenderingEnabled(boolean enabled) {
        INSTANCE.styledRenderingEnabledValue = enabled;
        INSTANCE.trySetConfigValue(INSTANCE.enableStyledRendering, enabled);
    }

    public static Set<String> screenBlacklist() {
        return INSTANCE.screenBlacklistValue;
    }

    public static void setScreenBlacklist(List<String> entries) {
        Set<String> normalised = normaliseEntries(entries);
        INSTANCE.screenBlacklistValue = Collections.unmodifiableSet(normalised);
        INSTANCE.trySetConfigValue(INSTANCE.blacklistScreens, List.copyOf(normalised));
    }

    public static Set<String> modIdBlacklist() {
        return INSTANCE.modIdBlacklistValue;
    }

    public static void setModIdBlacklist(List<String> entries) {
        Set<String> normalised = normaliseEntries(entries);
        INSTANCE.modIdBlacklistValue = Collections.unmodifiableSet(normalised);
        INSTANCE.trySetConfigValue(INSTANCE.blacklistModIds, List.copyOf(normalised));
    }

    public static int maxEffectsPerGlyph() {
        return INSTANCE.maxEffectsPerGlyphValue;
    }

    public static void setMaxEffectsPerGlyph(int value) {
        int clamped = clampNonNegative(value);
        INSTANCE.maxEffectsPerGlyphValue = clamped;
        INSTANCE.trySetConfigValue(INSTANCE.maxEffectsPerGlyph, clamped);
    }

    public static int maxSpanDepth() {
        return INSTANCE.maxSpanDepthValue;
    }

    public static void setMaxSpanDepth(int value) {
        int clamped = clampNonNegative(value);
        INSTANCE.maxSpanDepthValue = clamped;
        INSTANCE.trySetConfigValue(INSTANCE.maxSpanDepth, clamped);
    }

    public static boolean shouldBypassCurrentScreen() {
        if (!isStyledRenderingEnabled()) {
            return true;
        }
        if (isCurrentScreenBlacklisted()) {
            return true;
        }
        return isCurrentModBlacklisted();
    }

    public static int effectsVersion() {
        return Objects.hash(maxEffectsPerGlyph(), maxSpanDepth());
    }

    public static void resetToDefaults() {
        setStyledRenderingEnabled(true);
        setScreenBlacklist(List.of());
        setModIdBlacklist(List.of());
        setMaxEffectsPerGlyph(3);
        setMaxSpanDepth(8);
    }

    private static boolean isCurrentScreenBlacklisted() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = minecraft != null ? minecraft.screen : null;
            if (screen != null) {
                String name = screen.getClass().getName().toLowerCase(Locale.ROOT);
                return INSTANCE.screenBlacklistValue.contains(name);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean isCurrentModBlacklisted() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = minecraft != null ? minecraft.screen : null;
            if (screen != null) {
                return ModList.get().getModContainerByObject(screen)
                    .map(container -> container.getModId().toLowerCase(Locale.ROOT))
                    .map(INSTANCE.modIdBlacklistValue::contains)
                    .orElse(false);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static Set<String> normaliseEntries(List<? extends String> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptySet();
        }
        return entries.stream()
            .filter(Objects::nonNull)
            .map(entry -> entry.trim().toLowerCase(Locale.ROOT))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(HashSet::new));
    }

    private static int clampNonNegative(Integer value) {
        if (value == null) {
            return 0;
        }
        return Math.max(0, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void trySetConfigValue(ForgeConfigSpec.ConfigValue<?> target, Object value) {
        try {
            ((ForgeConfigSpec.ConfigValue) target).set(value);
        } catch (NullPointerException ignored) {
            // Tests may call setters before Forge has assigned a backing config instance.
        }
    }
}
