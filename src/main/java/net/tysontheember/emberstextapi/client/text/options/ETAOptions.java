package net.tysontheember.emberstextapi.client.text.options;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance.CaptionBasedToString;
import net.minecraft.client.OptionInstance.Enum;
import net.minecraft.client.OptionInstance.TooltipSupplier;
import net.minecraft.client.OptionInstance.UnitDouble;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.TypewriterController;

/**
 * Wraps the client OptionInstances that control global text animations.
 */
public final class ETAOptions {
    private static final double SPEED_MIN = 0.25D;
    private static final double SPEED_MAX = 5.0D;
    private static final DecimalFormat SPEED_FORMAT = new DecimalFormat("0.0x");

    private final OptionInstance<Boolean> animationEnabled;
    private final OptionInstance<TypewriterModeOption> typewriterMode;
    private final OptionInstance<Double> typewriterSpeed;

    private boolean suppressCallbacks;

    public ETAOptions() {
        this(Snapshot.DEFAULT);
    }

    public ETAOptions(Snapshot defaults) {
        Objects.requireNonNull(defaults, "defaults");
        TooltipSupplier<Boolean> animationTooltip = OptionInstance.cachedConstantTooltip(
                Component.translatable("options.emberstextapi.animation.tooltip"));
        this.animationEnabled = OptionInstance.createBoolean(
                "options.emberstextapi.animation",
                animationTooltip,
                OptionInstance.BOOLEAN_TO_STRING,
                defaults.animationEnabled(),
                value -> this.onChanged());

        TooltipSupplier<TypewriterModeOption> modeTooltip = OptionInstance.cachedConstantTooltip(
                Component.translatable("options.emberstextapi.typewriterMode.tooltip"));
        CaptionBasedToString<TypewriterModeOption> modeCaption = OptionInstance.forOptionEnum();
        Enum<TypewriterModeOption> modeValues = new Enum<>(
                List.of(TypewriterModeOption.values()),
                Codec.STRING.xmap(ETAOptions::decodeMode, TypewriterModeOption::getKey));
        TypewriterModeOption defaultMode = TypewriterModeOption.fromController(defaults.typewriterMode());
        this.typewriterMode = new OptionInstance<>(
                "options.emberstextapi.typewriterMode",
                modeTooltip,
                modeCaption,
                modeValues,
                defaultMode,
                value -> this.onChanged());

        TooltipSupplier<Double> speedTooltip = value -> Tooltip.create(
                Component.translatable("options.emberstextapi.typewriterSpeed.tooltip"));
        CaptionBasedToString<Double> speedCaption = (caption, value) -> Component.translatable(
                "options.emberstextapi.typewriterSpeed.value",
                SPEED_FORMAT.format(Mth.clamp(value, SPEED_MIN, SPEED_MAX)));
        var slider = UnitDouble.INSTANCE.xmap(
                ETAOptions::sliderToSpeed,
                ETAOptions::speedToSlider);
        this.typewriterSpeed = new OptionInstance<>(
                "options.emberstextapi.typewriterSpeed",
                speedTooltip,
                speedCaption,
                slider,
                Codec.doubleRange(SPEED_MIN, SPEED_MAX),
                (double) Mth.clamp(defaults.typewriterSpeed(), (float) SPEED_MIN, (float) SPEED_MAX),
                value -> this.onChanged());
    }

    public OptionInstance<Boolean> animationEnabledOption() {
        return this.animationEnabled;
    }

    public OptionInstance<TypewriterModeOption> typewriterModeOption() {
        return this.typewriterMode;
    }

    public OptionInstance<Double> typewriterSpeedOption() {
        return this.typewriterSpeed;
    }

    public Snapshot snapshot() {
        boolean animation = Boolean.TRUE.equals(this.animationEnabled.get());
        TypewriterController.Mode mode = this.typewriterMode.get().toControllerMode();
        double speed = Mth.clamp(this.typewriterSpeed.get(), SPEED_MIN, SPEED_MAX);
        return new Snapshot(animation, mode, (float) speed);
    }

    public void loadFromSnapshot(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        this.suppressCallbacks = true;
        try {
            this.animationEnabled.set(snapshot.animationEnabled());
            this.typewriterMode.set(TypewriterModeOption.fromController(snapshot.typewriterMode()));
            this.typewriterSpeed.set((double) Mth.clamp(snapshot.typewriterSpeed(), (float) SPEED_MIN, (float) SPEED_MAX));
        } finally {
            this.suppressCallbacks = false;
        }
        this.onChanged();
    }

    public void process(Options.FieldAccess access) {
        access.process("etaAnimations", this.animationEnabled);
        access.process("etaTypewriterMode", this.typewriterMode);
        access.process("etaTypewriterSpeed", this.typewriterSpeed);
    }

    private void onChanged() {
        if (this.suppressCallbacks) {
            return;
        }
        GlobalTextConfig.setOptions(this.snapshot());
    }

    private static TypewriterModeOption decodeMode(String key) {
        return Arrays.stream(TypewriterModeOption.values())
                .filter(value -> value.getKey().equals(key))
                .findFirst()
                .orElse(TypewriterModeOption.OFF);
    }

    private static double sliderToSpeed(double slider) {
        return Mth.lerp(Mth.clamp(slider, 0.0D, 1.0D), SPEED_MIN, SPEED_MAX);
    }

    private static double speedToSlider(double speed) {
        return Mth.clamp((speed - SPEED_MIN) / (SPEED_MAX - SPEED_MIN), 0.0D, 1.0D);
    }

    public enum TypewriterModeOption implements OptionEnum {
        OFF(0, "off", TypewriterController.Mode.OFF),
        BY_CHAR(1, "char", TypewriterController.Mode.BY_CHAR),
        BY_WORD(2, "word", TypewriterController.Mode.BY_WORD);

        private final int id;
        private final String key;
        private final TypewriterController.Mode controllerMode;

        TypewriterModeOption(int id, String key, TypewriterController.Mode controllerMode) {
            this.id = id;
            this.key = "options.emberstextapi.typewriterMode." + key;
            this.controllerMode = controllerMode;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        public TypewriterController.Mode toControllerMode() {
            return this.controllerMode;
        }

        public static TypewriterModeOption fromController(TypewriterController.Mode mode) {
            for (TypewriterModeOption value : values()) {
                if (value.controllerMode == mode) {
                    return value;
                }
            }
            return OFF;
        }
    }

    public record Snapshot(boolean animationEnabled, TypewriterController.Mode typewriterMode, float typewriterSpeed) {
        public static final Snapshot DEFAULT = new Snapshot(true, TypewriterController.Mode.OFF, 1.0f);

        public Snapshot {
            if (typewriterMode == null) {
                typewriterMode = TypewriterController.Mode.OFF;
            }
            if (!Float.isFinite(typewriterSpeed) || typewriterSpeed <= 0.0f) {
                typewriterSpeed = 1.0f;
            }
        }

        public boolean isTypewriterEnabled() {
            return this.animationEnabled && this.typewriterMode != TypewriterController.Mode.OFF;
        }
    }

}
