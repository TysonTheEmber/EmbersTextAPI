package net.tysontheember.emberstextapi.core.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;

/**
 * Helper methods for serialising {@link SpanEffectState} structures to JSON and network buffers.
 */
public final class SpanEffectStateCodec {
    public static final String JSON_KEY = "emberstextapi:span";

    private static final String FIELD_GRADIENT = "gradient";
    private static final String FIELD_TYPEWRITER = "typewriter";
    private static final String FIELD_SHAKES = "shakes";
    private static final String FIELD_GLYPH_EFFECTS = "glyph_effects";
    private static final String FIELD_ATTACHMENTS = "attachments";
    private static final String FIELD_CUSTOM = "custom";

    private static final String FIELD_COLORS = "colors";
    private static final String FIELD_REPEATING = "repeating";
    private static final String FIELD_SPEED = "speed";
    private static final String FIELD_OFFSET = "offset";
    private static final String FIELD_CENTERED = "centered";
    private static final String FIELD_TRACK = "track";
    private static final String FIELD_INDEX = "index";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_AMPLITUDE = "amplitude";
    private static final String FIELD_WAVELENGTH = "wavelength";
    private static final String FIELD_PER_GLYPH = "per_glyph";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NBT = "nbt";

    private SpanEffectStateCodec() {
    }

    public static JsonElement toJson(SpanEffectState state) {
        if (state == null || state.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject root = new JsonObject();
        SpanGradient gradient = state.gradient();
        if (gradient != null) {
            JsonObject jsonGradient = new JsonObject();
            JsonArray colors = new JsonArray();
            for (int color : gradient.colors()) {
                colors.add(color);
            }
            jsonGradient.add(FIELD_COLORS, colors);
            jsonGradient.addProperty(FIELD_REPEATING, gradient.repeating());
            jsonGradient.addProperty(FIELD_SPEED, gradient.speed());
            jsonGradient.addProperty(FIELD_OFFSET, gradient.offset());
            root.add(FIELD_GRADIENT, jsonGradient);
        }

        TypewriterState typewriter = state.typewriter();
        if (typewriter != null) {
            JsonObject jsonTypewriter = new JsonObject();
            jsonTypewriter.addProperty(FIELD_SPEED, typewriter.speed());
            jsonTypewriter.addProperty(FIELD_CENTERED, typewriter.centered());
            jsonTypewriter.addProperty(FIELD_TRACK, typewriter.track());
            jsonTypewriter.addProperty(FIELD_INDEX, typewriter.index());
            root.add(FIELD_TYPEWRITER, jsonTypewriter);
        }

        if (!state.shakes().isEmpty()) {
            JsonArray shakes = new JsonArray();
            state.shakes().forEach(shake -> {
                JsonObject jsonShake = new JsonObject();
                jsonShake.addProperty(FIELD_TYPE, shake.type().name());
                jsonShake.addProperty(FIELD_AMPLITUDE, shake.amplitude());
                jsonShake.addProperty(FIELD_SPEED, shake.speed());
                jsonShake.addProperty(FIELD_WAVELENGTH, shake.wavelength());
                jsonShake.addProperty(FIELD_PER_GLYPH, shake.perGlyph());
                shakes.add(jsonShake);
            });
            root.add(FIELD_SHAKES, shakes);
        }

        if (!state.glyphEffects().isEmpty()) {
            JsonArray effects = new JsonArray();
            state.glyphEffects().forEach(effect -> {
                JsonObject jsonEffect = new JsonObject();
                jsonEffect.addProperty(FIELD_ID, effect.id().toString());
                CompoundTag tag = effect.parameters();
                if (!tag.isEmpty()) {
                    jsonEffect.addProperty(FIELD_NBT, tag.toString());
                }
                effects.add(jsonEffect);
            });
            root.add(FIELD_GLYPH_EFFECTS, effects);
        }

        if (!state.attachments().isEmpty()) {
            JsonArray attachments = new JsonArray();
            state.attachments().forEach(attachment -> {
                JsonObject jsonAttachment = new JsonObject();
                jsonAttachment.addProperty(FIELD_ID, attachment.type().toString());
                CompoundTag payload = attachment.payload();
                if (!payload.isEmpty()) {
                    jsonAttachment.addProperty(FIELD_NBT, payload.toString());
                }
                attachments.add(jsonAttachment);
            });
            root.add(FIELD_ATTACHMENTS, attachments);
        }

        if (!state.customData().isEmpty()) {
            JsonObject custom = new JsonObject();
            for (Map.Entry<ResourceLocation, CompoundTag> entry : state.customData().entrySet()) {
                custom.add(entry.getKey().toString(), new JsonPrimitive(entry.getValue().toString()));
            }
            root.add(FIELD_CUSTOM, custom);
        }

        return root;
    }

    public static SpanEffectState fromJson(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        SpanEffectState state = new SpanEffectState();

        if (object.has(FIELD_GRADIENT)) {
            JsonObject gradientObj = object.getAsJsonObject(FIELD_GRADIENT);
            JsonArray colorsArray = gradientObj.has(FIELD_COLORS) ? gradientObj.getAsJsonArray(FIELD_COLORS) : null;
            if (colorsArray != null && !colorsArray.isEmpty()) {
                List<Integer> colors = new ArrayList<>();
                colorsArray.forEach(colorElement -> colors.add(colorElement.getAsInt()));
                boolean repeating = getBoolean(gradientObj, FIELD_REPEATING, false);
                float speed = getFloat(gradientObj, FIELD_SPEED, 0.0f);
                float offset = getFloat(gradientObj, FIELD_OFFSET, 0.0f);
                state.setGradient(new SpanGradient(colors, repeating, speed, offset));
            }
        }

        if (object.has(FIELD_TYPEWRITER)) {
            JsonObject typewriterObj = object.getAsJsonObject(FIELD_TYPEWRITER);
            float speed = getFloat(typewriterObj, FIELD_SPEED, 0.0f);
            boolean centered = getBoolean(typewriterObj, FIELD_CENTERED, false);
            int track = getInt(typewriterObj, FIELD_TRACK, 0);
            int index = getInt(typewriterObj, FIELD_INDEX, 0);
            state.setTypewriter(new TypewriterState(speed, centered, track, index));
        }

        if (object.has(FIELD_SHAKES)) {
            JsonArray shakes = object.getAsJsonArray(FIELD_SHAKES);
            shakes.forEach(json -> {
                JsonObject shakeObj = json.getAsJsonObject();
                String typeName = getString(shakeObj, FIELD_TYPE, null);
                if (typeName == null) {
                    return;
                }
                try {
                    var type = ShakeType.valueOf(typeName);
                    float amplitude = getFloat(shakeObj, FIELD_AMPLITUDE, 0.0f);
                    float speed = getFloat(shakeObj, FIELD_SPEED, 0.0f);
                    float wavelength = getFloat(shakeObj, FIELD_WAVELENGTH, 0.0f);
                    boolean perGlyph = getBoolean(shakeObj, FIELD_PER_GLYPH, false);
                    state.shakes().add(new ShakeState(type, amplitude, speed, wavelength, perGlyph));
                } catch (IllegalArgumentException ignored) {
                }
            });
        }

        if (object.has(FIELD_GLYPH_EFFECTS)) {
            JsonArray effects = object.getAsJsonArray(FIELD_GLYPH_EFFECTS);
            effects.forEach(json -> {
                JsonObject effectObj = json.getAsJsonObject();
                ResourceLocation id = parseResourceLocation(getString(effectObj, FIELD_ID, null));
                if (id == null) {
                    return;
                }
                CompoundTag tag = parseCompound(effectObj.get(FIELD_NBT));
                state.glyphEffects().add(new GlyphEffect(id, tag));
            });
        }

        if (object.has(FIELD_ATTACHMENTS)) {
            JsonArray attachments = object.getAsJsonArray(FIELD_ATTACHMENTS);
            attachments.forEach(json -> {
                JsonObject attachmentObj = json.getAsJsonObject();
                ResourceLocation id = parseResourceLocation(getString(attachmentObj, FIELD_ID, null));
                if (id == null) {
                    return;
                }
                CompoundTag payload = parseCompound(attachmentObj.get(FIELD_NBT));
                state.attachments().add(new InlineAttachment(id, payload));
            });
        }

        if (object.has(FIELD_CUSTOM)) {
            JsonObject custom = object.getAsJsonObject(FIELD_CUSTOM);
            for (Map.Entry<String, JsonElement> entry : custom.entrySet()) {
                ResourceLocation key = parseResourceLocation(entry.getKey());
                if (key == null) {
                    continue;
                }
                CompoundTag value = parseCompound(entry.getValue());
                state.putCustomData(key, value);
            }
        }

        return state.isEmpty() ? null : state;
    }

    public static void write(FriendlyByteBuf buffer, SpanEffectState state) {
        if (state == null || state.isEmpty()) {
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);

        SpanGradient gradient = state.gradient();
        buffer.writeBoolean(gradient != null);
        if (gradient != null) {
            List<Integer> colors = gradient.colors();
            buffer.writeVarInt(colors.size());
            colors.forEach(buffer::writeInt);
            buffer.writeBoolean(gradient.repeating());
            buffer.writeFloat(gradient.speed());
            buffer.writeFloat(gradient.offset());
        }

        TypewriterState typewriter = state.typewriter();
        buffer.writeBoolean(typewriter != null);
        if (typewriter != null) {
            buffer.writeFloat(typewriter.speed());
            buffer.writeBoolean(typewriter.centered());
            buffer.writeVarInt(typewriter.track());
            buffer.writeVarInt(typewriter.index());
        }

        buffer.writeVarInt(state.shakes().size());
        state.shakes().forEach(shake -> {
            buffer.writeUtf(shake.type().name(), 64);
            buffer.writeFloat(shake.amplitude());
            buffer.writeFloat(shake.speed());
            buffer.writeFloat(shake.wavelength());
            buffer.writeBoolean(shake.perGlyph());
        });

        buffer.writeVarInt(state.glyphEffects().size());
        state.glyphEffects().forEach(effect -> {
            buffer.writeResourceLocation(effect.id());
            buffer.writeNbt(effect.parameters());
        });

        buffer.writeVarInt(state.attachments().size());
        state.attachments().forEach(attachment -> {
            buffer.writeResourceLocation(attachment.type());
            buffer.writeNbt(attachment.payload());
        });

        buffer.writeVarInt(state.customData().size());
        state.customData().forEach((key, value) -> {
            buffer.writeResourceLocation(key);
            buffer.writeNbt(value);
        });
    }

    public static SpanEffectState read(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        SpanEffectState state = new SpanEffectState();

        if (buffer.readBoolean()) {
            int colorCount = buffer.readVarInt();
            List<Integer> colors = new ArrayList<>(colorCount);
            for (int i = 0; i < colorCount; i++) {
                colors.add(buffer.readInt());
            }
            boolean repeating = buffer.readBoolean();
            float speed = buffer.readFloat();
            float offset = buffer.readFloat();
            state.setGradient(new SpanGradient(colors, repeating, speed, offset));
        }

        if (buffer.readBoolean()) {
            float speed = buffer.readFloat();
            boolean centered = buffer.readBoolean();
            int track = buffer.readVarInt();
            int index = buffer.readVarInt();
            state.setTypewriter(new TypewriterState(speed, centered, track, index));
        }

        int shakeCount = buffer.readVarInt();
        for (int i = 0; i < shakeCount; i++) {
            String typeName = buffer.readUtf(64);
            try {
                var type = ShakeType.valueOf(typeName);
                float amplitude = buffer.readFloat();
                float speed = buffer.readFloat();
                float wavelength = buffer.readFloat();
                boolean perGlyph = buffer.readBoolean();
                state.shakes().add(new ShakeState(type, amplitude, speed, wavelength, perGlyph));
            } catch (IllegalArgumentException ignored) {
                buffer.readFloat();
                buffer.readFloat();
                buffer.readFloat();
                buffer.readBoolean();
            }
        }

        int effectCount = buffer.readVarInt();
        for (int i = 0; i < effectCount; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            CompoundTag data = buffer.readNbt();
            state.glyphEffects().add(new GlyphEffect(id, data));
        }

        int attachmentCount = buffer.readVarInt();
        for (int i = 0; i < attachmentCount; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            CompoundTag payload = buffer.readNbt();
            state.attachments().add(new InlineAttachment(id, payload));
        }

        int customCount = buffer.readVarInt();
        for (int i = 0; i < customCount; i++) {
            ResourceLocation key = buffer.readResourceLocation();
            CompoundTag value = buffer.readNbt();
            state.putCustomData(key, value);
        }

        return state.isEmpty() ? null : state;
    }

    private static boolean getBoolean(JsonObject object, String key, boolean fallback) {
        return object.has(key) ? object.get(key).getAsBoolean() : fallback;
    }

    private static float getFloat(JsonObject object, String key, float fallback) {
        return object.has(key) ? object.get(key).getAsFloat() : fallback;
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private static String getString(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }

    private static ResourceLocation parseResourceLocation(String value) {
        return value == null ? null : ResourceLocation.tryParse(value);
    }

    private static CompoundTag parseCompound(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return new CompoundTag();
        }
        try {
            return TagParser.parseTag(element.getAsString());
        } catch (CommandSyntaxException | UnsupportedOperationException e) {
            return new CompoundTag();
        }
    }
}
