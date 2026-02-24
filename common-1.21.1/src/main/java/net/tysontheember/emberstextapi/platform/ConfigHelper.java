package net.tysontheember.emberstextapi.platform;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Platform abstraction for configuration.
 */
public interface ConfigHelper {
    /**
     * Get the singleton instance.
     */
    static ConfigHelper getInstance() {
        return ConfigHelperImpl.INSTANCE;
    }

    /**
     * Register the mod configuration.
     * Called during mod initialization.
     */
    void register();

    /**
     * Check if the welcome message is enabled.
     */
    boolean isWelcomeMessageEnabled();

    /**
     * Check if immersive messages are enabled globally.
     */
    default boolean isImmersiveMessagesEnabled() {
        return true;
    }

    /**
     * Get the list of globally disabled effect names.
     */
    default List<String> getDisabledEffects() {
        return Collections.emptyList();
    }

    /**
     * Check if a specific effect is disabled.
     */
    default boolean isEffectDisabled(String effectName) {
        return getDisabledEffects().stream()
            .anyMatch(e -> e.equalsIgnoreCase(effectName));
    }

    /**
     * Get the markup permission mode: "NONE", "WHITELIST", or "BLACKLIST".
     * NONE = no restrictions, WHITELIST = only listed players can use markup,
     * BLACKLIST = listed players cannot use markup.
     */
    default String getMarkupPermissionMode() {
        return "NONE";
    }

    /**
     * Get the list of player UUIDs for the markup whitelist/blacklist.
     */
    default List<String> getMarkupPlayerList() {
        return Collections.emptyList();
    }

    /**
     * Check if a player is allowed to use markup in chat.
     */
    default boolean isPlayerAllowedMarkup(UUID playerUuid) {
        String mode = getMarkupPermissionMode();
        if ("NONE".equalsIgnoreCase(mode)) {
            return true;
        }
        String uuidStr = playerUuid.toString();
        boolean inList = getMarkupPlayerList().stream()
            .anyMatch(e -> e.equalsIgnoreCase(uuidStr));
        if ("WHITELIST".equalsIgnoreCase(mode)) {
            return inList;
        }
        if ("BLACKLIST".equalsIgnoreCase(mode)) {
            return !inList;
        }
        return true;
    }

    /**
     * Get the maximum immersive message duration in ticks. 0 = unlimited.
     */
    default int getMaxMessageDuration() {
        return 0;
    }

    /**
     * Get the maximum number of simultaneous active messages per player. 0 = unlimited.
     */
    default int getMaxActiveMessages() {
        return 0;
    }
}
