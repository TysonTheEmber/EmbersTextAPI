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
}
