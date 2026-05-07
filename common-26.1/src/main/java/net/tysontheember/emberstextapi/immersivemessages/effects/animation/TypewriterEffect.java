package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypewriterEffect extends BaseEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterEffect.class);

    private final int speedMs;

    @Nullable
    private final String sound;

    private final long resetDelayMs;

    private final int maxPlays;

    public TypewriterEffect(@NotNull Params params) {
        super(params);

        int rawSpeed = params.getDouble("speed")
                .or(() -> params.getDouble("s").map(s -> s > 0 ? 1000.0 / s : 1000.0))
                .map(Number::intValue)
                .filter(ms -> ms > 0)
                .orElse(20);
        this.speedMs = ValidationHelper.clamp("typewriter", "speed", rawSpeed, 1, 10000);

        this.sound = params.getString("sound").orElse(null);

        long rawResetDelay = params.getDouble("resetDelay")
                .map(seconds -> (long) (seconds * 1000))
                .filter(ms -> ms >= 0)
                .orElse(1000L);
        this.resetDelayMs = Math.min(rawResetDelay, 60000L);

        this.maxPlays = params.getBoolean("loop")
                .map(loop -> loop ? -1 : 1)
                .or(() -> params.getString("repeat").map(TypewriterEffect::parseRepeat))
                .orElse(-1);

        LOGGER.debug("TypewriterEffect created: speedMs={}, maxPlays={}, loop={}, repeat={}",
                speedMs, this.maxPlays,
                params.getBoolean("loop").orElse(null),
                params.getString("repeat").orElse("(not set)"));
    }

    private static int parseRepeat(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }

        String lower = value.toLowerCase().trim();

        if ("yes".equals(lower) || "true".equals(lower) || "infinite".equals(lower)) {
            return -1;
        }

        if ("no".equals(lower) || "false".equals(lower) || "once".equals(lower)) {
            return 1;
        }

        try {
            int n = Integer.parseInt(lower);
            return n <= 0 ? -1 : n;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        TypewriterTrack track = settings.typewriterTrack;
        if (track == null) {

            track = TypewriterTracks.getInstance().get(this);
        }

        track.setInterval(speedMs);
        track.setResetDelayMs(resetDelayMs);
        track.setMaxPlays(maxPlays);
        if (sound != null && track.getSound() == null) {
            track.setSound(sound);
        }

        if (settings.absoluteIndex == 0) {
            LOGGER.debug("TypewriterEffect.apply: effect.maxPlays={}, track.maxPlays={}, track.playCount={}, track.index={}, track.totalChars={}",
                    this.maxPlays, track.getMaxPlays(), track.getPlayCount(), track.index, track.getTotalChars());
        }

        if (track.isCompleted()) {

            if (settings.absoluteIndex == 0) {
                LOGGER.debug("Track already completed: maxPlays={}, playCount={}, totalChars={}",
                        track.getMaxPlays(), track.getPlayCount(), track.getTotalChars());
            }
            return;
        }

        track.checkAndResetIfNeeded();

        track.update();

        if (settings.absoluteIndex >= track.index) {
            settings.a = 0.0f;
        }
    }

    public int getSpeedMs() {
        return speedMs;
    }

    @Nullable
    public String getSound() {
        return sound;
    }

    public long getResetDelayMs() {
        return resetDelayMs;
    }

    public int getMaxPlays() {
        return maxPlays;
    }

    @NotNull
    @Override
    public String getName() {
        return "typewriter";
    }
}
