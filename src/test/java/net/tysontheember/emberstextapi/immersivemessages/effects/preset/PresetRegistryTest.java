package net.tysontheember.emberstextapi.immersivemessages.effects.preset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PresetRegistryTest {

    @AfterEach
    void tearDown() {
        PresetRegistry.clear();
    }

    @Test
    public void testRegisterAndGet() {
        PresetDefinition preset = new PresetDefinition("testpreset", 1,
                List.of(new PresetDefinition.EffectEntry("rainbow")), null);

        PresetRegistry.register(preset);

        PresetDefinition retrieved = PresetRegistry.get("testpreset");
        assertNotNull(retrieved, "Should retrieve registered preset");
        assertEquals("testpreset", retrieved.getName());
    }

    @Test
    public void testGetReturnsNullForUnregistered() {
        assertNull(PresetRegistry.get("nonexistent"), "Should return null for unregistered preset");
    }

    @Test
    public void testCaseInsensitiveLookup() {
        PresetDefinition preset = new PresetDefinition("MyPreset", 1,
                List.of(new PresetDefinition.EffectEntry("rainbow")), null);

        PresetRegistry.register(preset);

        assertNotNull(PresetRegistry.get("mypreset"), "Lowercase lookup should work");
        assertNotNull(PresetRegistry.get("MYPRESET"), "Uppercase lookup should work");
        assertNotNull(PresetRegistry.get("MyPreset"), "Mixed case lookup should work");
    }

    @Test
    public void testClearRemovesAll() {
        PresetRegistry.register(new PresetDefinition("alpha", 1,
                List.of(new PresetDefinition.EffectEntry("rainbow")), null));
        PresetRegistry.register(new PresetDefinition("beta", 1,
                List.of(new PresetDefinition.EffectEntry("neon")), null));

        assertTrue(PresetRegistry.isPreset("alpha"));
        assertTrue(PresetRegistry.isPreset("beta"));

        PresetRegistry.clear();

        assertFalse(PresetRegistry.isPreset("alpha"), "Preset 'alpha' should be gone after clear");
        assertFalse(PresetRegistry.isPreset("beta"), "Preset 'beta' should be gone after clear");
    }

    @Test
    public void testIsReservedForBuiltInTags() {
        assertTrue(PresetRegistry.isReserved("bold"), "'bold' is a reserved tag");
        assertTrue(PresetRegistry.isReserved("rainbow"), "'rainbow' is a reserved tag");
        assertTrue(PresetRegistry.isReserved("item"), "'item' is a reserved tag");
        assertTrue(PresetRegistry.isReserved("shake"), "'shake' is a reserved tag");
        assertTrue(PresetRegistry.isReserved("background"), "'background' is a reserved tag");
    }

    @Test
    public void testIsReservedIsCaseInsensitive() {
        assertTrue(PresetRegistry.isReserved("BOLD"), "Reserved check should be case-insensitive");
        assertTrue(PresetRegistry.isReserved("Rainbow"), "Reserved check should be case-insensitive");
    }

    @Test
    public void testIsNotReservedForCustomNames() {
        assertFalse(PresetRegistry.isReserved("legendary"), "'legendary' is not reserved");
        assertFalse(PresetRegistry.isReserved("epic"), "'epic' is not reserved");
        assertFalse(PresetRegistry.isReserved("mysupereffect"), "'mysupereffect' is not reserved");
    }

    @Test
    public void testRegisterThrowsForReservedName() {
        PresetDefinition preset = new PresetDefinition("bold", 1,
                List.of(new PresetDefinition.EffectEntry("rainbow")), null);

        assertThrows(IllegalArgumentException.class, () -> PresetRegistry.register(preset),
                "Registering a preset with a reserved name should throw");
    }

    @Test
    public void testIsPreset() {
        assertFalse(PresetRegistry.isPreset("legendary"));

        PresetRegistry.register(new PresetDefinition("legendary", 1,
                List.of(new PresetDefinition.EffectEntry("rainbow")), null));

        assertTrue(PresetRegistry.isPreset("legendary"));
    }

    @Test
    public void testGetRegisteredPresets() {
        PresetRegistry.register(new PresetDefinition("one", 1,
                List.of(new PresetDefinition.EffectEntry("rainbow")), null));
        PresetRegistry.register(new PresetDefinition("two", 1,
                List.of(new PresetDefinition.EffectEntry("neon")), null));

        var names = PresetRegistry.getRegisteredPresets();
        assertEquals(2, names.size(), "Should have 2 registered presets");
        assertTrue(names.contains("one"));
        assertTrue(names.contains("two"));
    }
}
