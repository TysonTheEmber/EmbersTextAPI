package net.tysontheember.emberstextapi.debug;

/**
 * Collection of base debug events used throughout the API. Individual systems are expected
 * to contribute additional event types in later commits.
 */
public final class DebugEvents {
    private DebugEvents() {
    }

    /**
     * Creates a new config reload event.
     */
    public static ConfigReloaded configReloaded() {
        return new ConfigReloaded();
    }

    public static DebugModeChanged debugModeChanged(boolean enabled, String source) {
        return new DebugModeChanged(enabled, source);
    }

    public static TraceChannelToggled traceChannelToggled(DebugFlags.TraceChannel channel, boolean enabled, String source) {
        return new TraceChannelToggled(channel, enabled, source);
    }

    public static OverlayVisibilityChanged overlayVisibilityChanged(boolean visible, String source) {
        return new OverlayVisibilityChanged(visible, source);
    }

    public static OverlayLevelChanged overlayLevelChanged(int level, String source) {
        return new OverlayLevelChanged(level, source);
    }

    public static PerfModeChanged perfModeChanged(boolean enabled, String source) {
        return new PerfModeChanged(enabled, source);
    }

    public static FailSafeModeChanged failSafeModeChanged(boolean enabled, String source) {
        return new FailSafeModeChanged(enabled, source);
    }

    public static DebugDumpRequested debugDumpRequested(String source) {
        return new DebugDumpRequested(source);
    }

    /**
     * Event emitted whenever the debug configuration is reloaded.
     */
    public static final class ConfigReloaded extends DebugEvent {
        private ConfigReloaded() {
        }

        @Override
        public String describe() {
            return "Debug configuration reloaded";
        }
    }

    public static final class DebugModeChanged extends DebugEvent {
        private final boolean enabled;
        private final String source;

        private DebugModeChanged(boolean enabled, String source) {
            this.enabled = enabled;
            this.source = source;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String describe() {
            return "Debug mode " + (enabled ? "enabled" : "disabled") + " via " + source;
        }
    }

    public static final class TraceChannelToggled extends DebugEvent {
        private final DebugFlags.TraceChannel channel;
        private final boolean enabled;
        private final String source;

        private TraceChannelToggled(DebugFlags.TraceChannel channel, boolean enabled, String source) {
            this.channel = channel;
            this.enabled = enabled;
            this.source = source;
        }

        public DebugFlags.TraceChannel getChannel() {
            return channel;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String describe() {
            return "Trace channel " + channel + " " + (enabled ? "enabled" : "disabled") + " via " + source;
        }
    }

    public static final class OverlayVisibilityChanged extends DebugEvent {
        private final boolean visible;
        private final String source;

        private OverlayVisibilityChanged(boolean visible, String source) {
            this.visible = visible;
            this.source = source;
        }

        public boolean isVisible() {
            return visible;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String describe() {
            return "Debug overlay " + (visible ? "shown" : "hidden") + " via " + source;
        }
    }

    public static final class OverlayLevelChanged extends DebugEvent {
        private final int level;
        private final String source;

        private OverlayLevelChanged(int level, String source) {
            this.level = level;
            this.source = source;
        }

        public int getLevel() {
            return level;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String describe() {
            return "Debug overlay level set to " + level + " via " + source;
        }
    }

    public static final class PerfModeChanged extends DebugEvent {
        private final boolean enabled;
        private final String source;

        private PerfModeChanged(boolean enabled, String source) {
            this.enabled = enabled;
            this.source = source;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String describe() {
            return "Performance timers " + (enabled ? "enabled" : "disabled") + " via " + source;
        }
    }

    public static final class FailSafeModeChanged extends DebugEvent {
        private final boolean enabled;
        private final String source;

        private FailSafeModeChanged(boolean enabled, String source) {
            this.enabled = enabled;
            this.source = source;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String describe() {
            return "Fail-safe mode " + (enabled ? "enabled" : "disabled") + " via " + source;
        }
    }

    public static final class DebugDumpRequested extends DebugEvent {
        private final String source;

        private DebugDumpRequested(String source) {
            this.source = source;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String describe() {
            return "Debug dump requested via " + source;
        }
    }
}
