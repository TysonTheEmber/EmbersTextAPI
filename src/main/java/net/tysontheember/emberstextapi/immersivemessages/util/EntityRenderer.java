package net.tysontheember.emberstextapi.immersivemessages.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

/**
 * Utility class for rendering entities in GUI contexts with full control over
 * rotation, scale, offset, and lighting.
 */
public final class EntityRenderer {
    private EntityRenderer() {}

    /**
     * Renders an entity with the specified parameters.
     *
     * @param graphics   The GUI graphics context
     * @param entityId   The entity's resource location (e.g., "minecraft:creeper")
     * @param x          Base X position
     * @param y          Base Y position
     * @param scale      Scale multiplier (1.0 = default size)
     * @param offsetX    Additional X offset in pixels
     * @param offsetY    Additional Y offset in pixels
     * @param yaw        Y-axis rotation in degrees (horizontal rotation)
     * @param pitch      X-axis rotation in degrees (vertical tilt)
     * @param roll       Z-axis rotation in degrees (barrel roll)
     * @param lighting   Light level 0-15 (15 = full bright, lower = darker with shading)
     * @return The width of the rendered entity for layout purposes, or 0 if rendering failed
     */
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
        return render(graphics, entityId, x, y, scale, offsetX, offsetY, yaw, pitch, roll, lighting, null);
    }

    /**
     * Renders an entity with the specified parameters and optional spin animation.
     *
     * @param graphics   The GUI graphics context
     * @param entityId   The entity's resource location (e.g., "minecraft:creeper")
     * @param x          Base X position
     * @param y          Base Y position
     * @param scale      Scale multiplier (1.0 = default size)
     * @param offsetX    Additional X offset in pixels
     * @param offsetY    Additional Y offset in pixels
     * @param yaw        Y-axis rotation in degrees (horizontal rotation)
     * @param pitch      X-axis rotation in degrees (vertical tilt)
     * @param roll       Z-axis rotation in degrees (barrel roll)
     * @param lighting   Light level 0-15 (15 = full bright, lower = darker with shading)
     * @param spin       Spin speed in degrees per tick (positive=clockwise, negative=counter-clockwise), null for no spin
     * @return The width of the rendered entity for layout purposes, or 0 if rendering failed
     */
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
            @Nullable Float spin
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0;

        try {
            ResourceLocation location = ResourceLocation.tryParse(entityId);
            if (location == null) return 0;

            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(location);
            if (entityType == null) return 0;

            Entity entity = entityType.create(mc.level);
            if (entity == null) return 0;

            // Apply spin animation to yaw if spin is set
            float finalYaw = yaw;
            if (spin != null && spin != 0) {
                // Use real wall-clock time for smooth animation that works in all contexts
                // Convert nanoseconds to "ticks" (1 tick = 50ms = 50,000,000 ns)
                // This ensures animation works even when game is paused or in menus
                float timeInTicks = (System.nanoTime() / 50_000_000f);
                finalYaw = yaw + (timeInTicks * spin);
                // Normalize to 0-360 range to prevent float overflow over time
                finalYaw = finalYaw % 360f;
            }

            return renderEntity(graphics, entity, x, y, scale, offsetX, offsetY, finalYaw, pitch, roll, lighting);
        } catch (Exception e) {
            // If rendering fails, silently skip
            return 0;
        }
    }

    /**
     * Renders an entity instance with full control over transformation and lighting.
     *
     * @param graphics   The GUI graphics context
     * @param entity     The entity to render
     * @param x          Base X position
     * @param y          Base Y position
     * @param scale      Scale multiplier
     * @param offsetX    Additional X offset
     * @param offsetY    Additional Y offset
     * @param yaw        Y-axis rotation in degrees
     * @param pitch      X-axis rotation in degrees
     * @param roll       Z-axis rotation in degrees
     * @param lighting   Light level 0-15
     * @return The width of the rendered entity
     */
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

        // Calculate entity size based on scale
        int entitySize = (int)(16 * scale);

        // Clamp lighting to valid range
        int clampedLight = Math.max(0, Math.min(15, lighting));

        // Build packed light value
        // LightTexture.pack(blockLight, skyLight) - we use lighting for both to get consistent shading
        int packedLight = LightTexture.pack(clampedLight, clampedLight);

        graphics.pose().pushPose();

        // Position the entity
        // X: base position + custom offset + half entity width for centering
        // Y: base position + custom offset + entity size (entities render from bottom)
        // Z: 100 for proper depth ordering in GUI
        graphics.pose().translate(
                x + offsetX + entitySize / 2.0f,
                y + offsetY + entitySize,
                100
        );

        // Apply scale (multiply by 10 for appropriate GUI scale)
        float renderScale = scale * 10;
        graphics.pose().scale(renderScale, renderScale, renderScale);

        // Apply rotations in order: flip upright, then yaw, pitch, roll
        // First flip 180 degrees on X to make entities render upright
        graphics.pose().mulPose(Axis.XP.rotationDegrees(180));

        // Yaw (Y-axis rotation) - 180 offset so 0 = facing viewer
        graphics.pose().mulPose(Axis.YP.rotationDegrees(180 + yaw));

        // Pitch (X-axis rotation) - vertical tilt
        graphics.pose().mulPose(Axis.XP.rotationDegrees(pitch));

        // Roll (Z-axis rotation) - barrel roll
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(roll));

        // Render the entity
        var entityRenderDispatcher = mc.getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false); // Disable shadow for GUI rendering

        entityRenderDispatcher.render(
                entity,
                0, 0, 0,  // Entity position (relative, transformations handle actual position)
                0,        // Entity yaw (we handle rotation via PoseStack)
                0,        // Partial ticks
                graphics.pose(),
                mc.renderBuffers().bufferSource(),
                packedLight
        );

        // Flush the render buffer
        mc.renderBuffers().bufferSource().endBatch();

        entityRenderDispatcher.setRenderShadow(true); // Re-enable shadow for normal rendering

        graphics.pose().popPose();

        return entitySize;
    }

    /**
     * Renders an entity with default lighting (full bright).
     */
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

    /**
     * Simple render with just entity ID, position, scale, and rotation.
     */
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
