package net.tysontheember.emberstextapi.immersivemessages.effects.preset;

import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PresetLoaderTest {

    @BeforeAll
    static void setUp() {
        // Initialize built-in effects so validation can check effect types
        EffectRegistry.initializeDefaultEffects();
    }

    @AfterEach
    void tearDown() {
        PresetRegistry.clear();
    }

    @Test
    public void testParseValidPreset() {
        String json = """
                {
                  "format_version": 1,
                  "effects": [
                    { "type": "rainbow", "params": { "f": 1.5, "w": 0.8 } },
                    { "type": "neon" }
                  ],
                  "styles": {
                    "bold": true,
                    "color": "FFD700"
                  }
                }
                """;

        PresetDefinition preset = PresetLoader.parseJson("legendary", json);

        assertNotNull(preset, "Valid JSON should parse successfully");
        assertEquals("legendary", preset.getName());
        assertEquals(1, preset.getFormatVersion());
        assertEquals(2, preset.getEffects().size(), "Should have 2 effects");

        assertEquals("rainbow", preset.getEffects().get(0).type());
        assertEquals(1.5, preset.getEffects().get(0).params().get("f"));
        assertEquals(0.8, preset.getEffects().get(0).params().get("w"));

        assertEquals("neon", preset.getEffects().get(1).type());
        assertTrue(preset.getEffects().get(1).params().isEmpty(), "Neon should have no params");

        assertNotNull(preset.getStyles());
        assertTrue(preset.getStyles().bold());
        assertEquals("FFD700", preset.getStyles().color());
        assertNull(preset.getStyles().italic(), "Italic should be null (not specified)");
    }

    @Test
    public void testParseEffectsOnly() {
        String json = """
                {
                  "format_version": 1,
                  "effects": [
                    { "type": "wave" }
                  ]
                }
                """;

        PresetDefinition preset = PresetLoader.parseJson("simple", json);

        assertNotNull(preset, "Preset with only effects should parse");
        assertNull(preset.getStyles(), "Styles should be null when not specified");
        assertEquals(1, preset.getEffects().size());
    }

    @Test
    public void testRejectsReservedName() {
        String json = """
                {
                  "format_version": 1,
                  "effects": [{ "type": "rainbow" }]
                }
                """;

        assertNull(PresetLoader.parseJson("bold", json), "Should reject reserved name 'bold'");
        assertNull(PresetLoader.parseJson("rainbow", json), "Should reject reserved name 'rainbow'");
    }

    @Test
    public void testRejectsMissingFormatVersion() {
        String json = """
                {
                  "effects": [{ "type": "rainbow" }]
                }
                """;

        assertNull(PresetLoader.parseJson("test", json), "Should reject missing format_version");
    }

    @Test
    public void testRejectsWrongFormatVersion() {
        String json = """
                {
                  "format_version": 99,
                  "effects": [{ "type": "rainbow" }]
                }
                """;

        assertNull(PresetLoader.parseJson("test", json), "Should reject unsupported format_version");
    }

    @Test
    public void testRejectsMissingEffects() {
        String json = """
                {
                  "format_version": 1
                }
                """;

        assertNull(PresetLoader.parseJson("test", json), "Should reject missing effects array");
    }

    @Test
    public void testRejectsEmptyEffects() {
        String json = """
                {
                  "format_version": 1,
                  "effects": []
                }
                """;

        assertNull(PresetLoader.parseJson("test", json), "Should reject empty effects array");
    }

    @Test
    public void testRejectsUnknownEffectType() {
        String json = """
                {
                  "format_version": 1,
                  "effects": [{ "type": "nonexistent_effect" }]
                }
                """;

        assertNull(PresetLoader.parseJson("test", json), "Should reject unknown effect type");
    }

    @Test
    public void testRejectsMissingEffectType() {
        String json = """
                {
                  "format_version": 1,
                  "effects": [{ "params": { "f": 1.0 } }]
                }
                """;

        assertNull(PresetLoader.parseJson("test", json), "Should reject effect entry without type");
    }

    @Test
    public void testRejectsInvalidJson() {
        assertNull(PresetLoader.parseJson("test", "not json at all"),
                "Should reject malformed JSON");
    }

    @Test
    public void testParsesAllStyleOverrides() {
        String json = """
                {
                  "format_version": 1,
                  "effects": [{ "type": "rainbow" }],
                  "styles": {
                    "bold": true,
                    "italic": false,
                    "underline": true,
                    "strikethrough": false,
                    "obfuscated": true,
                    "color": "FF0000",
                    "font": "minecraft:alt"
                  }
                }
                """;

        PresetDefinition preset = PresetLoader.parseJson("styled", json);

        assertNotNull(preset);
        PresetDefinition.StyleOverrides styles = preset.getStyles();
        assertNotNull(styles);
        assertTrue(styles.bold());
        assertFalse(styles.italic());
        assertTrue(styles.underline());
        assertFalse(styles.strikethrough());
        assertTrue(styles.obfuscated());
        assertEquals("FF0000", styles.color());
        assertEquals("minecraft:alt", styles.font());
    }

    @Test
    public void testEffectParamsTypes() {
        String json = """
                {
                  "format_version": 1,
                  "effects": [
                    { "type": "rainbow", "params": { "f": 2.5, "enabled": true, "label": "test" } }
                  ]
                }
                """;

        PresetDefinition preset = PresetLoader.parseJson("typed", json);

        assertNotNull(preset);
        var params = preset.getEffects().get(0).params();
        assertEquals(2.5, params.get("f"), "Numeric param should be double");
        assertEquals(true, params.get("enabled"), "Boolean param should be boolean");
        assertEquals("test", params.get("label"), "String param should be string");
    }
}
