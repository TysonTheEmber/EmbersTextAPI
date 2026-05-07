package net.tysontheember.emberstextapi.platform;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ConfigHelper {
    static ConfigHelper getInstance() {
        return ConfigHelperImpl.INSTANCE;
    }

    void register();

    default boolean isImmersiveMessagesEnabled() {
        return true;
    }

    default List<String> getDisabledEffects() {
        return Collections.emptyList();
    }

    default boolean isEffectDisabled(String effectName) {
        if (getDisabledEffects().stream().anyMatch(e -> e.equalsIgnoreCase(effectName))) {
            return true;
        }
        return isReduceMotionEnabled() && isMotionEffect(effectName);
    }

    static boolean isMotionEffect(String effectName) {
        Set<String> motionEffects = Set.of(
            "wave", "bounce", "swing", "shake", "circle",
            "wiggle", "pend", "pendulum", "turb", "turbulence"
        );
        return motionEffects.contains(effectName.toLowerCase());
    }

    default String getMarkupPermissionMode() {
        return "NONE";
    }

    default List<String> getMarkupPlayerList() {
        return Collections.emptyList();
    }

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

    default int getMaxMessageDuration() {
        return 0;
    }

    default int getMaxActiveMessages() {
        return 0;
    }

    default int getAnvilNameMaxLength() {
        return 50;
    }

    default int getMaxServerMessageDuration() {
        return 1200;
    }

    default int getMaxServerActiveMessages() {
        return 10;
    }

    default int getMaxQueueSize() {
        return 50;
    }

    default List<String> getAllowedEffects() {
        return Collections.emptyList();
    }

    default List<String> getDisallowedMarkupTags() {
        return Collections.emptyList();
    }

    default boolean isReduceMotionEnabled() {
        return false;
    }

    default int getMaxNeonQuality() {
        return 3;
    }

    default int getTextLayoutCacheSize() {
        return 256;
    }

    default boolean isSdfEnabled() {
        return true;
    }
}
