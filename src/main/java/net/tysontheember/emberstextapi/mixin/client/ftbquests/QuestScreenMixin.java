package net.tysontheember.emberstextapi.mixin.client.ftbquests;

import net.tysontheember.emberstextapi.util.ViewStateTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
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

    /**
     * Track the last quest context to detect changes.
     * Only mark quest as viewed when it changes, not every frame.
     */
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
                System.out.println("QUEST MIXIN: Quest context changed to: " + questContext);
                ViewStateTracker.markQuestViewed(questContext);
                emberstextapi$lastQuestContext = questContext;
            }

        } catch (Exception e) {
            System.err.println("QUEST MIXIN ERROR: " + e.getMessage());
            e.printStackTrace();
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
        // Try to extract quest-specific information
        // This is a best-effort approach; exact implementation depends on FTB Quests internals

        try {
            // Attempt to get current quest ID via reflection or duck interface
            // For now, use a generic context that will reset on every screen render
            // This ensures typewriter effect resets when quest screen is opened
            return "quest:ftb:" + System.identityHashCode(this);

        } catch (Exception e) {
            // Fall back to generic quest context
            return "quest:ftb:generic";
        }
    }
}
