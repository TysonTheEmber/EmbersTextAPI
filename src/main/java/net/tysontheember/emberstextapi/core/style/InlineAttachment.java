package net.tysontheember.emberstextapi.core.style;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents an inline attachment such as an item or entity render.
 */
public final class InlineAttachment {
    private final ResourceLocation type;
    private final CompoundTag payload;

    public InlineAttachment(ResourceLocation type) {
        this(type, new CompoundTag());
    }

    public InlineAttachment(ResourceLocation type, CompoundTag payload) {
        this.type = Objects.requireNonNull(type, "type");
        this.payload = payload == null ? new CompoundTag() : payload.copy();
    }

    public ResourceLocation type() {
        return type;
    }

    public CompoundTag payload() {
        return payload.copy();
    }

    public InlineAttachment withPayload(CompoundTag payload) {
        return new InlineAttachment(type, payload);
    }

    public InlineAttachment copy() {
        return new InlineAttachment(type, payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InlineAttachment that)) {
            return false;
        }
        return type.equals(that.type) && payload.equals(that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, payload);
    }

    @Override
    public String toString() {
        return "InlineAttachment{" +
                "type=" + type +
                ", payload=" + payload +
                '}';
    }
}
