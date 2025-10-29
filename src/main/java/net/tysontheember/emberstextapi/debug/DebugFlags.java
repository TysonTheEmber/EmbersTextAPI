package net.tysontheember.emberstextapi.debug;

import java.util.EnumMap;
import java.util.Map;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * In-memory view of the debug configuration. All read access to configuration values
 * should go through this class to ensure consistency between the config file and the
 * runtime feature flags.
 */
public final class DebugFlags {
    public enum TraceChannel {
        PARSE,
        LAYOUT,
        RENDER,
        CACHE,
        MIXIN
    }

    private static final Map<TraceChannel, Boolean> TRACE_FLAGS = new EnumMap<>(TraceChannel.class);

    private static volatile boolean debugEnabled;
    private static volatile boolean overlayEnabled;
    private static volatile int overlayLevel;
    private static volatile boolean perfEnabled;
    private static volatile boolean failSafeOnError;
    private static volatile boolean spanEverywhere;
    private static volatile int effectsVersion;

    private DebugFlags() {
    }

    public static void reload() {
        boolean configLoaded = DebugConfig.SPEC.isLoaded();
        boolean debug = getValue(DebugConfig.DEBUG_ENABLED, configLoaded);
        boolean traceParse = getValue(DebugConfig.TRACE_PARSE, configLoaded);
        boolean traceLayout = getValue(DebugConfig.TRACE_LAYOUT, configLoaded);
        boolean traceRender = getValue(DebugConfig.TRACE_RENDER, configLoaded);
        boolean traceCache = getValue(DebugConfig.TRACE_CACHE, configLoaded);
        boolean traceMixin = getValue(DebugConfig.TRACE_MIXIN, configLoaded);
        boolean overlay = getValue(DebugConfig.OVERLAY_ENABLED, configLoaded);
        int overlayLvl = getValue(DebugConfig.OVERLAY_LEVEL, configLoaded);
        boolean perf = getValue(DebugConfig.PERF_ENABLED, configLoaded);
        boolean failSafe = getValue(DebugConfig.FAIL_SAFE_ON_ERROR, configLoaded);
        boolean spans = getValue(DebugConfig.SPAN_EVERYWHERE, configLoaded);
        int effects = getValue(DebugConfig.EFFECTS_VERSION, configLoaded);

        reloadFrom(debug, traceParse, traceLayout, traceRender, traceCache, traceMixin, overlay, overlayLvl, perf, failSafe, spans, effects);
    }

    static void reloadFrom(boolean debug,
                           boolean traceParse,
                           boolean traceLayout,
                           boolean traceRender,
                           boolean traceCache,
                           boolean traceMixin,
                           boolean overlay,
                           int overlayLvl,
                           boolean perf,
                           boolean failSafe,
                           boolean spans,
                           int effects) {
        debugEnabled = debug;
        TRACE_FLAGS.put(TraceChannel.PARSE, traceParse);
        TRACE_FLAGS.put(TraceChannel.LAYOUT, traceLayout);
        TRACE_FLAGS.put(TraceChannel.RENDER, traceRender);
        TRACE_FLAGS.put(TraceChannel.CACHE, traceCache);
        TRACE_FLAGS.put(TraceChannel.MIXIN, traceMixin);

        overlayEnabled = overlay;
        overlayLevel = overlayLvl;
        perfEnabled = perf;
        failSafeOnError = failSafe;
        spanEverywhere = spans;
        effectsVersion = effects;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static boolean isTraceEnabled(TraceChannel channel) {
        if (!debugEnabled) {
            return false;
        }
        return TRACE_FLAGS.getOrDefault(channel, Boolean.FALSE);
    }

    public static boolean isOverlayEnabled() {
        return debugEnabled && overlayEnabled;
    }

    public static int getOverlayLevel() {
        return overlayLevel;
    }

    public static boolean isPerfEnabled() {
        return debugEnabled && perfEnabled;
    }

    public static boolean isFailSafeOnError() {
        return failSafeOnError;
    }

    public static boolean isSpanEverywhere() {
        return spanEverywhere;
    }

    public static int getEffectsVersion() {
        return effectsVersion;
    }

    private static boolean getValue(ForgeConfigSpec.BooleanValue value, boolean configLoaded) {
        return configLoaded ? value.get() : value.getDefault();
    }

    private static int getValue(ForgeConfigSpec.IntValue value, boolean configLoaded) {
        return configLoaded ? value.get() : value.getDefault();
    }

    static {
        for (TraceChannel channel : TraceChannel.values()) {
            TRACE_FLAGS.put(channel, Boolean.FALSE);
        }
        reload();
    }
}
