package net.tysontheember.emberstextapi.mixin.client.ftbquests;

import net.tysontheember.emberstextapi.client.ViewStateTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
    "dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen",
    "dev.ftb.mods.ftbquests.client.gui.QuestScreen"
}, remap = false)
public class QuestScreenMixin {

    @Unique
    private static final Logger emberstextapi$LOGGER = LoggerFactory.getLogger("EmbersTextAPI/QuestScreenMixin");

    @Unique
    private String emberstextapi$lastQuestContext = null;

    @Inject(
        method = "render",
        at = @At("HEAD"),
        require = 0,
        remap = false
    )
    private void emberstextapi$onQuestScreenRender(CallbackInfo ci) {
        try {

            String questContext = emberstextapi$getQuestContext();

            if (!questContext.equals(emberstextapi$lastQuestContext)) {
                emberstextapi$LOGGER.debug("Quest context changed to: {}", questContext);
                ViewStateTracker.markQuestViewed(questContext);
                emberstextapi$lastQuestContext = questContext;
            }

        } catch (Exception e) {
            emberstextapi$LOGGER.debug("Failed to track quest context: {}", e.getMessage());
        }
    }

    private String emberstextapi$getQuestContext() {

        Object quest = null;
        try {
            quest = emberstextapi$getField(this, "selectedQuest");
            if (quest == null) quest = emberstextapi$getField(this, "quest");
            if (quest == null) quest = emberstextapi$getField(this, "focusedQuest");
            if (quest == null) quest = emberstextapi$getField(this, "viewedQuest");
        } catch (Exception ignored) {
        }

        if (quest != null) {
            try {
                Object id = quest.getClass().getMethod("getId").invoke(quest);
                if (id != null) {
                    return "quest:ftb:" + id.toString();
                }
            } catch (Exception ignored) {
            }
            try {
                Object id = quest.getClass().getField("id").get(quest);
                if (id != null) {
                    return "quest:ftb:" + id.toString();
                }
            } catch (Exception ignored) {
            }
        }

        return "quest:ftb:" + System.identityHashCode(this);
    }

    @Unique
    private Object emberstextapi$getField(Object target, String name) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException e) {

            return null;
        } catch (Exception e) {
            emberstextapi$LOGGER.debug("Could not access field '{}': {}", name, e.getMessage());
            return null;
        }
    }
}
