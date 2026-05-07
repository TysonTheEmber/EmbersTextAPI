package net.tysontheember.emberstextapi.immersivemessages.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.joml.Matrix4f;

import org.jetbrains.annotations.Nullable;

public final class EntityRenderer {
    private EntityRenderer() {}

    public static int render(
            GuiGraphics graphics,
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
            GuiGraphics graphics,
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
            ResourceLocation location = ResourceLocation.tryParse(entityId);
            if (location == null) return 0;

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(location);
            if (entityType == null) return 0;

            Entity entity = entityType.create(mc.level);
            if (entity == null) return 0;

            if (nbt != null && !nbt.isEmpty()) {
                try {
                    CompoundTag nbtTag = TagParser.parseTag(nbt);
                    entity.load(nbtTag);
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
            GuiGraphics graphics,
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
        Minecraft mc = Minecraft.getInstance();

        int entitySize = (int)(16 * scale);

        int clampedLight = Math.max(0, Math.min(15, lighting));

        int packedLight = LightTexture.pack(clampedLight, clampedLight);

        graphics.pose().pushPose();

        graphics.pose().translate(
                x + offsetX + entitySize / 2.0f,
                y + offsetY + entitySize,
                100
        );

        float renderScale = scale * 10;
        graphics.pose().scale(renderScale, renderScale, renderScale);

        graphics.pose().mulPose(Axis.XP.rotationDegrees(180));

        graphics.pose().mulPose(Axis.YP.rotationDegrees(180 + yaw));

        graphics.pose().mulPose(Axis.XP.rotationDegrees(pitch));

        graphics.pose().mulPose(Axis.ZP.rotationDegrees(roll));

        var entityRenderDispatcher = mc.getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);

        entityRenderDispatcher.render(
                entity,
                0, 0, 0,
                0,
                0,
                graphics.pose(),
                mc.renderBuffers().bufferSource(),
                packedLight
        );

        mc.renderBuffers().bufferSource().endBatch();

        entityRenderDispatcher.setRenderShadow(true);

        graphics.pose().popPose();

        return entitySize;
    }

    public static int render(
            GuiGraphics graphics,
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
            GuiGraphics graphics,
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
