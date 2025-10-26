package net.tysontheember.emberstextapi.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.tysontheember.emberstextapi.core.style.InlineAttachment;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;

@OnlyIn(Dist.CLIENT)
public final class InlineAttachmentRenderer {
    private static final ResourceLocation ATTACHMENT_ITEM = ResourceLocation.fromNamespaceAndPath("emberstextapi", "item");
    private static final ResourceLocation ATTACHMENT_ENTITY = ResourceLocation.fromNamespaceAndPath("emberstextapi", "entity");
    private static final float ITEM_SIZE = 16.0f;
    private static final float DEFAULT_PADDING = 2.0f;

    private InlineAttachmentRenderer() {
    }

    public static float render(Font font, GlyphRenderSettings settings, MultiBufferSource buffers, int packedLight) {
        SpanEffectState state = settings.effectState();
        if (state == null || state.attachments().isEmpty() || !settings.visible() || settings.pose() == null) {
            return 0.0f;
        }

        float advance = 0.0f;
        for (InlineAttachment attachment : state.attachments()) {
            ResourceLocation type = attachment.type();
            if (ATTACHMENT_ITEM.equals(type)) {
                advance += renderItem(font, settings, buffers, packedLight, attachment, advance);
            } else if (ATTACHMENT_ENTITY.equals(type)) {
                advance += renderEntity(font, settings, buffers, packedLight, attachment, advance);
            }
        }
        return advance;
    }

    private static float renderItem(Font font, GlyphRenderSettings settings, MultiBufferSource buffers, int packedLight,
            InlineAttachment attachment, float advance) {
        CompoundTag payload = attachment.payload();
        String idString = payload.getString("id");
        if (idString.isEmpty()) {
            return 0.0f;
        }
        ResourceLocation id = ResourceLocation.tryParse(idString);
        if (id == null) {
            return 0.0f;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            return 0.0f;
        }

        int count = payload.contains("count") ? Math.max(payload.getInt("count"), 1) : 1;
        ItemStack stack = new ItemStack(item, count);
        float offsetX = payload.contains("offset_x") ? payload.getFloat("offset_x") : 0.0f;
        float offsetY = payload.contains("offset_y") ? payload.getFloat("offset_y") : 0.0f;

        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(settings.pose());
        poseStack.pushPose();

        float baseX = settings.renderX() + advance + offsetX;
        float baseY = settings.renderY() + offsetY - (ITEM_SIZE - font.lineHeight) / 2.0f;
        poseStack.translate(baseX, baseY, 50.0f);
        poseStack.translate(8.0f, 8.0f, 0.0f);
        poseStack.scale(1.0f, -1.0f, 1.0f);
        poseStack.scale(16.0f, 16.0f, 16.0f);

        minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, packedLight, OverlayTexture.NO_OVERLAY, poseStack,
                buffers, minecraft.level, 0);

        poseStack.popPose();
        return ITEM_SIZE + DEFAULT_PADDING;
    }

    private static float renderEntity(Font font, GlyphRenderSettings settings, MultiBufferSource buffers, int packedLight,
            InlineAttachment attachment, float advance) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0.0f;
        }

        CompoundTag payload = attachment.payload();
        String idString = payload.getString("id");
        if (idString.isEmpty()) {
            return 0.0f;
        }
        ResourceLocation id = ResourceLocation.tryParse(idString);
        if (id == null) {
            return 0.0f;
        }

        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null) {
            return 0.0f;
        }

        Entity entity = type.create(minecraft.level);
        if (entity == null) {
            return 0.0f;
        }

        float scale = payload.contains("scale") ? payload.getFloat("scale") : 1.0f;
        float offsetX = payload.contains("offset_x") ? payload.getFloat("offset_x") : 0.0f;
        float offsetY = payload.contains("offset_y") ? payload.getFloat("offset_y") : 0.0f;
        float yaw = payload.contains("yaw") ? payload.getFloat("yaw") : 45.0f;
        float pitch = payload.contains("pitch") ? payload.getFloat("pitch") : 0.0f;
        float size = ITEM_SIZE * Math.max(scale, 0.01f);

        entity.setYRot(180.0f + yaw);
        entity.setXRot(pitch);
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();

        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(settings.pose());
        poseStack.pushPose();

        float baseX = settings.renderX() + advance + offsetX;
        float baseY = settings.renderY() + offsetY - (size - font.lineHeight) / 2.0f;
        poseStack.translate(baseX + size / 2.0f, baseY + size, 50.0f);
        poseStack.scale(scale * 10.0f, scale * 10.0f, scale * 10.0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f + yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        dispatcher.render(entity, 0.0d, 0.0d, 0.0d, 0.0f, 0.0f, poseStack, buffers, packedLight);

        poseStack.popPose();
        return size + DEFAULT_PADDING;
    }
}
