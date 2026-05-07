package net.tysontheember.emberstextapi.immersivemessages.effects.preset;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PresetDefinition {

    private final String name;
    private final int formatVersion;
    private final List<EffectEntry> effects;
    @Nullable
    private final StyleOverrides styles;

    public PresetDefinition(@NotNull String name, int formatVersion,
                            @NotNull List<EffectEntry> effects,
                            @Nullable StyleOverrides styles) {
        this.name = name;
        this.formatVersion = formatVersion;
        this.effects = Collections.unmodifiableList(effects);
        this.styles = styles;
    }

    public @NotNull String getName() {
        return name;
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public @NotNull List<EffectEntry> getEffects() {
        return effects;
    }

    public @Nullable StyleOverrides getStyles() {
        return styles;
    }

    public record EffectEntry(@NotNull String type, @NotNull Map<String, Object> params) {
        public EffectEntry(@NotNull String type, @NotNull Map<String, Object> params) {
            this.type = type;
            this.params = Collections.unmodifiableMap(params);
        }

        public EffectEntry(@NotNull String type) {
            this(type, Collections.emptyMap());
        }
    }

    public record StyleOverrides(
            @Nullable Boolean bold,
            @Nullable Boolean italic,
            @Nullable Boolean underline,
            @Nullable Boolean strikethrough,
            @Nullable Boolean obfuscated,
            @Nullable String color,
            @Nullable String font
    ) {}
}
