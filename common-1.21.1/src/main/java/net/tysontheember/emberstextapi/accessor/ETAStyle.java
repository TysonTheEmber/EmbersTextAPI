package net.tysontheember.emberstextapi.accessor;

import com.google.common.collect.ImmutableList;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTrack;

public interface ETAStyle {

    ImmutableList<Effect> emberstextapi$getEffects();
    void emberstextapi$setEffects(ImmutableList<Effect> effects);
    void emberstextapi$addEffect(Effect effect);

    String emberstextapi$getItemId();
    void emberstextapi$setItemId(String itemId);
    Integer emberstextapi$getItemCount();
    void emberstextapi$setItemCount(Integer count);
    Float emberstextapi$getItemOffsetX();
    void emberstextapi$setItemOffsetX(Float offsetX);
    Float emberstextapi$getItemOffsetY();
    void emberstextapi$setItemOffsetY(Float offsetY);
    String emberstextapi$getItemNbt();
    void emberstextapi$setItemNbt(String nbt);

    TypewriterTrack emberstextapi$getTypewriterTrack();
    void emberstextapi$setTypewriterTrack(TypewriterTrack track);
    int emberstextapi$getTypewriterIndex();
    void emberstextapi$setTypewriterIndex(int index);

    Object emberstextapi$getObfuscateKey();
    void emberstextapi$setObfuscateKey(Object key);
    Object emberstextapi$getObfuscateStableKey();
    void emberstextapi$setObfuscateStableKey(Object key);
    int emberstextapi$getObfuscateSpanStart();
    void emberstextapi$setObfuscateSpanStart(int start);
    int emberstextapi$getObfuscateSpanLength();
    void emberstextapi$setObfuscateSpanLength(int length);

    String emberstextapi$getEntityId();
    void emberstextapi$setEntityId(String entityId);
    Float emberstextapi$getEntityScale();
    void emberstextapi$setEntityScale(Float scale);
    Float emberstextapi$getEntityOffsetX();
    void emberstextapi$setEntityOffsetX(Float offsetX);
    Float emberstextapi$getEntityOffsetY();
    void emberstextapi$setEntityOffsetY(Float offsetY);
    Float emberstextapi$getEntityYaw();
    void emberstextapi$setEntityYaw(Float yaw);
    Float emberstextapi$getEntityPitch();
    void emberstextapi$setEntityPitch(Float pitch);
    Float emberstextapi$getEntityRoll();
    void emberstextapi$setEntityRoll(Float roll);
    Integer emberstextapi$getEntityLighting();
    void emberstextapi$setEntityLighting(Integer lighting);
    Float emberstextapi$getEntitySpin();
    void emberstextapi$setEntitySpin(Float spin);
    String emberstextapi$getEntityAnimation();
    void emberstextapi$setEntityAnimation(String animation);
    String emberstextapi$getEntityNbt();
    void emberstextapi$setEntityNbt(String nbt);
}
