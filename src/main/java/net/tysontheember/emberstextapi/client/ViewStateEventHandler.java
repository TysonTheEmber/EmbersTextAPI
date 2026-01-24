package net.tysontheember.emberstextapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.util.ViewStateTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side event handler for tracking text visibility state.
 * <p>
 * This handler monitors client events to detect when text becomes visible,
 * triggering view state updates in {@link ViewStateTracker}. This enables
 * effects like typewriter to reset their animations when tooltips appear
 * or screens open.
 * </p>
 *
 * <h3>Tracked Events:</h3>
 * <ul>
 *   <li>Tooltip rendering (item hover)</li>
 *   <li>Screen opening/closing</li>
 *   <li>Client ticks (for cleanup and monitoring)</li>
 * </ul>
 *
 * <h3>Integration:</h3>
 * <p>
 * This event handler is automatically registered by the mod on the Forge event bus
 * during client initialization. No manual registration needed.
 * </p>
 *
 * @see ViewStateTracker
 * @see net.tysontheember.emberstextapi.immersivemessages.effects.visual.TypewriterEffect
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ViewStateEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewStateEventHandler.class);

    /**
     * Currently tracked item stack for tooltip context.
     * Used to generate unique context IDs for different items.
     */
    private static ItemStack lastTooltipStack = ItemStack.EMPTY;

    /**
     * Cached empty tooltip context with timestamp.
     * Reused within the same tooltip session to allow animation to progress.
     */
    private static String cachedEmptyTooltipContext = null;

    /**
     * Last time a tooltip was rendered (milliseconds).
     * Used to detect gaps in tooltip rendering.
     */
    private static long lastTooltipTime = 0;

    /**
     * Currently tracked screen for context.
     * Used to detect screen changes.
     */
    private static Screen lastScreen = null;

    /**
     * Handle tooltip pre-render event to track tooltip visibility.
     * <p>
     * This event fires before a tooltip is rendered, allowing us to detect
     * when tooltips appear and change. We generate a context ID based on
     * the item being hovered.
     * </p>
     *
     * @param event The tooltip pre-render event
     */
    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        try {
            ItemStack stack = event.getItemStack();
            long currentTime = System.currentTimeMillis();

            // Clear cached empty tooltip context when there's a gap in tooltip rendering
            // This happens when you move mouse between quests (brief moment with no tooltip)
            boolean shouldClearCache = false;

            if (stack.isEmpty()) {
                // Hovering over quest icon (empty stack)
                long timeSinceLastTooltip = currentTime - lastTooltipTime;

                if (timeSinceLastTooltip > 100) {
                    // Gap detected (>100ms since last tooltip) - must be a new hover
                    shouldClearCache = true;
                    LOGGER.debug("Cleared cache: tooltip gap detected ({}ms)", timeSinceLastTooltip);
                } else if (!lastTooltipStack.isEmpty()) {
                    // Transitioned from item to quest - clear cache
                    shouldClearCache = true;
                    LOGGER.debug("Cleared cache: transitioned from item to quest");
                }
            } else {
                // Hovering over real item - clear cache for next quest hover
                if (cachedEmptyTooltipContext != null) {
                    shouldClearCache = true;
                    LOGGER.debug("Cleared cache: hovering real item");
                }
            }

            if (shouldClearCache) {
                cachedEmptyTooltipContext = null;
            }

            // Update last tooltip time
            lastTooltipTime = currentTime;

            // Generate tooltip context ID from item
            String tooltipContext = generateTooltipContext(stack);

            LOGGER.debug("Tooltip pre-render for context: {}", tooltipContext);

            // Update view state tracker
            ViewStateTracker.updateTooltipContext(tooltipContext);

            // Track for change detection
            lastTooltipStack = stack.copy();

        } catch (Exception e) {
            LOGGER.error("Error handling tooltip pre-render event", e);
        }
    }

    /**
     * Handle screen open event to track screen visibility.
     * <p>
     * This event fires when a new screen is opened, including:
     * <ul>
     *   <li>Inventory screens</li>
     *   <li>Quest screens (FTB Quests, etc.)</li>
     *   <li>Custom GUI screens</li>
     * </ul>
     * </p>
     *
     * @param event The screen open event
     */
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        try {
            Screen newScreen = event.getNewScreen();

            if (newScreen != null) {
                String screenContext = generateScreenContext(newScreen);
                ViewStateTracker.markScreenOpened(screenContext);
                lastScreen = newScreen;

                LOGGER.debug("Screen opened: {}", screenContext);
            }

        } catch (Exception e) {
            LOGGER.error("Error handling screen open event", e);
        }
    }

    /**
     * Handle screen close event to track screen visibility.
     * <p>
     * This event fires when a screen is closed.
     * </p>
     *
     * @param event The screen close event
     */
    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        try {
            ViewStateTracker.markScreenClosed();
            lastScreen = null;

            LOGGER.trace("Screen closed");

        } catch (Exception e) {
            LOGGER.error("Error handling screen close event", e);
        }
    }

    /**
     * Handle client tick to detect tooltip state changes.
     * <p>
     * This runs every client tick to detect when tooltips disappear
     * (no tooltip render events). We clear the tooltip context when
     * no tooltip is being rendered.
     * </p>
     *
     * @param event The client tick event
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        try {
            Minecraft mc = Minecraft.getInstance();

            // Check if we're still on the same screen
            if (mc.screen != lastScreen) {
                if (lastScreen != null) {
                    // Screen changed without firing close event (shouldn't happen, but be safe)
                    ViewStateTracker.markScreenClosed();
                }

                if (mc.screen != null) {
                    String screenContext = generateScreenContext(mc.screen);
                    ViewStateTracker.markScreenOpened(screenContext);
                }

                lastScreen = mc.screen;
            }

            // Clear cached empty tooltip context when screen changes
            // This ensures fresh context when reopening quest screen
            if (mc.screen != lastScreen) {
                cachedEmptyTooltipContext = null;
            }

            // Note: Tooltip state is handled by RenderTooltipEvent, not here
            // The tooltip events fire per-frame during rendering

        } catch (Exception e) {
            LOGGER.error("Error handling client tick event", e);
        }
    }

    /**
     * Generate a unique context identifier for a tooltip based on the item stack.
     * <p>
     * Context format: "tooltip:item_id[:count][:nbt_hash]"
     * </p>
     *
     * @param stack The item stack being hovered
     * @return Context identifier string
     */
    private static String generateTooltipContext(ItemStack stack) {
        if (stack.isEmpty()) {
            // For empty stacks (like quest icons), generate a unique context
            // But reuse it during the same hover session so animation can progress
            if (cachedEmptyTooltipContext == null) {
                cachedEmptyTooltipContext = "tooltip:empty:" + System.currentTimeMillis();
                LOGGER.debug("Generated new empty tooltip context: {}", cachedEmptyTooltipContext);
            }
            return cachedEmptyTooltipContext;
        }

        // Clear cached empty tooltip when hovering over real items
        cachedEmptyTooltipContext = null;

        StringBuilder context = new StringBuilder("tooltip:");
        context.append(stack.getItem().toString());

        // Include count if stack size matters for tooltip
        if (stack.getCount() > 1) {
            context.append(":").append(stack.getCount());
        }

        // Include NBT hash if item has special data
        if (stack.hasTag()) {
            context.append(":nbt").append(stack.getTag().hashCode());
        }

        return context.toString();
    }

    /**
     * Generate a unique context identifier for a screen.
     * <p>
     * Context format: "screen:ClassName"
     * </p>
     * <p>
     * For FTB Quests screens, we try to extract quest-specific information
     * if available.
     * </p>
     *
     * @param screen The screen being opened
     * @return Context identifier string
     */
    private static String generateScreenContext(Screen screen) {
        String className = screen.getClass().getSimpleName();
        String fullClassName = screen.getClass().getName();

        LOGGER.debug("Screen context: class={}, fullClass={}", className, fullClassName);

        // Check if this is an FTB Quests screen (by package or class name)
        if (fullClassName.contains("ftbquests") || className.contains("Quest")) {
            // Try to extract quest ID if possible
            // This is a best-effort approach; exact implementation depends on FTB Quests version
            try {
                // For now, use class name + title hash as context
                String title = screen.getTitle().getString();
                LOGGER.debug("Quest screen detected, title: {}", title);
                if (!title.isEmpty()) {
                    // Use "quest:" prefix for compatibility with existing quest context tracking
                    return "quest:screen:" + title.hashCode();
                }
            } catch (Exception e) {
                LOGGER.warn("Error extracting quest title", e);
            }
        }

        return "screen:" + className;
    }

    /**
     * Reset all view state tracking.
     * <p>
     * Used for cleanup when leaving a world or on mod reload.
     * </p>
     */
    public static void reset() {
        lastTooltipStack = ItemStack.EMPTY;
        lastScreen = null;
        cachedEmptyTooltipContext = null;
        ViewStateTracker.clear();
        LOGGER.debug("View state event handler reset");
    }

    /**
     * Clear the cached empty tooltip context.
     * Called when tooltip disappears to ensure fresh context on next hover.
     */
    public static void clearEmptyTooltipCache() {
        if (cachedEmptyTooltipContext != null) {
            LOGGER.debug("Clearing cached empty tooltip context: {}", cachedEmptyTooltipContext);
            cachedEmptyTooltipContext = null;
        }
    }
}
