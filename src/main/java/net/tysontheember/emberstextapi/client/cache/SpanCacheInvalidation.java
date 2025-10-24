package net.tysontheember.emberstextapi.client.cache;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.config.ClientSettings;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Objects;

/**
 * Centralised cache invalidation for span-aware text layouts.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT)
public final class SpanCacheInvalidation {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile String lastLanguageCode;
    private static volatile int lastGuiScale = Integer.MIN_VALUE;
    private static volatile int lastConfigFingerprint = Integer.MIN_VALUE;

    private SpanCacheInvalidation() {
    }

    static void resetForTests() {
        lastLanguageCode = null;
        lastGuiScale = Integer.MIN_VALUE;
        lastConfigFingerprint = Integer.MIN_VALUE;
    }

    static void handleLanguageCandidate(String languageCode) {
        if (languageCode == null) {
            return;
        }
        String normalised = languageCode.toLowerCase(Locale.ROOT);
        if (!Objects.equals(normalised, lastLanguageCode)) {
            lastLanguageCode = normalised;
            clearSpanCaches("language change -> " + normalised);
        }
    }

    static void handleGuiScaleCandidate(int guiScale) {
        if (guiScale != lastGuiScale) {
            lastGuiScale = guiScale;
            clearSpanCaches("GUI scale change -> " + guiScale);
        }
    }

    static void handleConfigFingerprintCandidate(int fingerprint) {
        if (fingerprint != lastConfigFingerprint) {
            lastConfigFingerprint = fingerprint;
            clearSpanCaches("client settings change");
        }
    }

    public static void clearSpanCaches(String reason) {
        LOGGER.debug("Clearing span layout caches due to {}", reason);
        TextLayoutCache.getInstance().clear();
        net.tysontheember.emberstextapi.client.TextLayoutCache.clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        LanguageManager languageManager = minecraft.getLanguageManager();
        if (languageManager != null) {
            try {
                handleLanguageCandidate(languageManager.getSelected());
            } catch (Throwable ignored) {
            }
        }
        try {
            OptionInstance<Integer> option = minecraft.options != null ? minecraft.options.guiScale() : null;
            if (option != null) {
                Integer value = option.get();
                if (value != null) {
                    handleGuiScaleCandidate(value);
                }
            }
        } catch (Throwable ignored) {
        }
        handleConfigFingerprintCandidate(computeConfigFingerprint());
    }

    private static int computeConfigFingerprint() {
        int blacklistScreensHash = ClientSettings.screenBlacklist().hashCode();
        int blacklistModsHash = ClientSettings.modIdBlacklist().hashCode();
        return Objects.hash(
            ClientSettings.isStyledRenderingEnabled(),
            ClientSettings.maxEffectsPerGlyph(),
            ClientSettings.maxSpanDepth(),
            blacklistScreensHash,
            blacklistModsHash
        );
    }

    private static boolean shouldHandleConfigEvent(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        return config.getModId().equals(EmbersTextAPI.MODID) && config.getType() == ModConfig.Type.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusListeners {
        private ModBusListeners() {
        }

        @SubscribeEvent
        public static void onConfigLoading(ModConfigEvent.Loading event) {
            if (shouldHandleConfigEvent(event)) {
                handleConfigFingerprintCandidate(computeConfigFingerprint());
            }
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            if (shouldHandleConfigEvent(event)) {
                handleConfigFingerprintCandidate(computeConfigFingerprint());
            }
        }
    }
}
