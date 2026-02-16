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

/**
 * Mixin for FTB Quests screens to track quest description visibility.
 * <p>
 * This is a pseudo-mixin that only applies when FTB Quests is present.
 * It intercepts quest screen rendering to detect when quest descriptions
 * become visible, triggering typewriter animation resets.
 * </p>
 * <p>
 * The {@code @Pseudo} annotation makes this mixin optional - it won't cause
 * errors if FTB Quests is not installed.
 * </p>
 *
 * <h3>FTB Quests Integration:</h3>
 * <p>
 * This mixin targets the FTB Quests screen classes to detect when:
 * <ul>
 *   <li>A quest panel is opened</li>
 *   <li>A different quest is selected</li>
 *   <li>Quest descriptions are being rendered</li>
 * </ul>
 * </p>
 *
 * <h3>Version Compatibility:</h3>
 * <p>
 * Targets FTB Quests for Minecraft 1.20.1 (Forge).
 * May need updates for different FTB Quests versions.
 * </p>
 */
@Pseudo
@Mixin(targets = {
    "dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen",
    "dev.ftb.mods.ftbquests.client.gui.QuestScreen"
}, remap = false)
public class QuestScreenMixin {

    @Unique
    private static final Logger emberstextapi$LOGGER = LoggerFactory.getLogger("EmbersTextAPI/QuestScreenMixin");

    /**
     * Track the last quest context to detect changes.
     * Only mark quest as viewed when it changes, not every frame.
     */
    @Unique
    private String emberstextapi$lastQuestContext = null;

    /**
     * Track when a quest screen renders.
     * <p>
     * This method is called during screen rendering, allowing us to detect
     * when quest descriptions become visible.
     * </p>
     * <p>
     * Note: The exact method name may vary by FTB Quests version.
     * Using broad injection point to maximize compatibility.
     * </p>
     */
    @Inject(
        method = "render",
        at = @At("HEAD"),
        require = 0,
        remap = false
    )
    private void emberstextapi$onQuestScreenRender(CallbackInfo ci) {
        try {
            // Extract quest context if possible
            String questContext = emberstextapi$getQuestContext();

            // Only mark as viewed if this is a NEW quest context (changed from last frame)
            if (!questContext.equals(emberstextapi$lastQuestContext)) {
                emberstextapi$LOGGER.debug("Quest context changed to: {}", questContext);
                ViewStateTracker.markQuestViewed(questContext);
                emberstextapi$lastQuestContext = questContext;
            }

        } catch (Exception e) {
            emberstextapi$LOGGER.debug("Failed to track quest context: {}", e.getMessage());
        }
    }

    /**
     * Extract quest context identifier from the current screen state.
     * <p>
     * Attempts to get quest ID or other unique identifier.
     * Falls back to generic identifier if quest-specific info isn't available.
     * </p>
     *
     * @return Quest context identifier
     */
    private String emberstextapi$getQuestContext() {
        // Try to extract a stable quest identifier via reflection.
        // We attempt a few common field/method names used by FTB Quests screens.
        Object quest = null;
        try {
            quest = emberstextapi$getField(this, "selectedQuest");
            if (quest == null) quest = emberstextapi$getField(this, "quest");
            if (quest == null) quest = emberstextapi$getField(this, "focusedQuest");
            if (quest == null) quest = emberstextapi$getField(this, "viewedQuest");
        } catch (Exception ignored) {
        }

        // If we found a quest object, try to pull its ID
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

        // Fallbacks if we cannot introspect quest
        return "quest:ftb:" + System.identityHashCode(this);
    }

    /**
     * Simple reflective field getter that logs failures at debug level.
     */
    @Unique
    private Object emberstextapi$getField(Object target, String name) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException e) {
            // Field doesn't exist in this version - expected for version compatibility
            return null;
        } catch (Exception e) {
            emberstextapi$LOGGER.debug("Could not access field '{}': {}", name, e.getMessage());
            return null;
        }
    }
}
