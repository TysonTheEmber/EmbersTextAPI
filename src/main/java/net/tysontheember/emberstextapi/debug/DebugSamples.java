package net.tysontheember.emberstextapi.debug;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Centralised collection of markup samples used by the /eta debug harness.
 */
public final class DebugSamples {
    public static final String SAMPLE_NBT_KEY = "eta.sample";

    private static final Map<String, String> SAMPLES;

    static {
        Map<String, String> samples = new LinkedHashMap<>();
        samples.put("plain", "Hello from <bold>Embers</bold> API");
        samples.put("bold", "<bold>EMBERCRAFT</bold> demo");
        samples.put("italic", "<italic>slanted text</italic>");
        samples.put("underline", "<underlined>underline</underlined>");
        samples.put("strike", "<strikethrough>crossed</strikethrough>");
        samples.put("obf", "<obfuscated>secret</obfuscated>");
        samples.put("color", "<color #ff6a00>ember orange</color>");
        samples.put("grad", "<grad from=#ff6a00 to=#ffd500>EMBERCRAFT</grad>");
        samples.put("type", "<type speed=10 restartOnHover=true>typewriter test</type>");
        samples.put("combo", "<grad from=#ff6a00 to=#ffd500><bold><type speed=12 restartOnHover=true>Combo demo</type></bold></grad>");
        SAMPLES = Collections.unmodifiableMap(samples);
    }

    private DebugSamples() {
    }

    public static Set<String> keys() {
        return SAMPLES.keySet();
    }

    public static Optional<String> get(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(SAMPLES.get(key.toLowerCase(Locale.ROOT)));
    }

    public static String require(String key) {
        return get(key).orElseThrow(() -> new IllegalArgumentException("Unknown sample '" + key + "'"));
    }
}
