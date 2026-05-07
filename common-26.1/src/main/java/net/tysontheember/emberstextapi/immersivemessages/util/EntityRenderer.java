package net.tysontheember.emberstextapi.immersivemessages.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import org.jetbrains.annotations.Nullable;

public final class EntityRenderer {
    private EntityRenderer() {}

    public static int render(
            GuiGraphicsExtractor graphics,
            String entityId,
            float x,
            float y,
            float scale,
            float offsetX,
            float offsetY,
            float yaw,
            float pitch,
            float roll,
            int lighting
    ) {
        return render(graphics, entityId, x, y, scale, offsetX, offsetY, yaw, pitch, roll, lighting, null, null);
    }

    public static int render(
            GuiGraphicsExtractor graphics,
            String entityId,
            float x,
            float y,
            float scale,
            float offsetX,
            float offsetY,
            float yaw,
            float pitch,
            float roll,
            int lighting,
            @Nullable Float spin,
            @Nullable String nbt
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0;

        try {
            Identifier location = Identifier.tryParse(entityId);
            if (location == null) return 0;

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(location);
            if (entityType == null) return 0;

            Entity entity = entityType.create(mc.level, EntitySpawnReason.LOAD);
            if (entity == null) return 0;

            if (nbt != null && !nbt.isEmpty()) {
                try {
                    CompoundTag nbtTag = TagParser.parseCompoundFully(nbt);
                    net.minecraft.world.level.storage.ValueInput valueInput =
                            net.minecraft.world.level.storage.TagValueInput.create(
                                    net.minecraft.util.ProblemReporter.DISCARDING, mc.level.registryAccess(), nbtTag);
                    entity.load(valueInput);
                } catch (Exception e) {

                }
            }

            float finalYaw = yaw;
            if (spin != null && spin != 0) {

                float timeInTicks = (System.nanoTime() / 50_000_000f);
                finalYaw = yaw + (timeInTicks * spin);

                finalYaw = finalYaw % 360f;
            }

            return renderEntity(graphics, entity, x, y, scale, offsetX, offsetY, finalYaw, pitch, roll, lighting);
        } catch (Exception e) {

            return 0;
        }
    }

    public static int renderEntity(
            GuiGraphicsExtractor graphics,
            Entity entity,
            float x,
            float y,
            float scale,
            float offsetX,
            float offsetY,
            float yaw,
            float pitch,
            float roll,
            int lighting
    ) {

        int entitySize = (int)(16 * scale);

        return entitySize;
    }

    public static int render(
            GuiGraphicsExtractor graphics,
            String entityId,
            float x,
            float y,
            float scale,
            float offsetX,
            float offsetY,
            float yaw,
            float pitch,
            float roll
    ) {
        return render(graphics, entityId, x, y, scale, offsetX, offsetY, yaw, pitch, roll, 15);
    }

    public static int render(
            GuiGraphicsExtractor graphics,
            String entityId,
            float x,
            float y,
            float scale,
            float yaw,
            float pitch
    ) {
        return render(graphics, entityId, x, y, scale, 0, 0, yaw, pitch, 0, 15);
    }
}
