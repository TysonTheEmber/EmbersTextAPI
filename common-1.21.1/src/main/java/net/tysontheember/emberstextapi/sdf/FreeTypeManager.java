package net.tysontheember.emberstextapi.sdf;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Outline;
import org.lwjgl.util.freetype.FT_Outline_Funcs;
import org.lwjgl.util.freetype.FT_Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;

import static org.lwjgl.util.freetype.FreeType.*;

/**
 * Manages the FreeType library lifecycle for MSDF glyph rendering.
 * All FreeType calls are synchronized on this instance (FreeType is not thread-safe).
 * <p>
 * Provides outline extraction via {@code FT_Outline_Decompose} for the MSDF pipeline.
 * The bitmap rendering path (EDT-based SDF) has been removed in favor of direct
 * outline-based MSDF generation via {@link MSDFGenerator}.
 */
public final class FreeTypeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/FreeType");
    private static final FreeTypeManager INSTANCE = new FreeTypeManager();

    private long library;
    private boolean available;
    private boolean initialized;

    private FreeTypeManager() {}

    public static FreeTypeManager getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isAvailable() {
        if (!initialized) {
            initialize();
        }
        return available;
    }

    private void initialize() {
        initialized = true;
        try {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pLibrary = stack.mallocPointer(1);
                int error = FT_Init_FreeType(pLibrary);
                if (error != 0) {
                    LOGGER.warn("Failed to initialize FreeType library: error {}", error);
                    available = false;
                    return;
                }
                library = pLibrary.get(0);
            }
            available = true;
            LOGGER.info("FreeType library initialized successfully");
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            LOGGER.warn("FreeType native library not available: {}", e.getMessage());
            available = false;
        } catch (Throwable t) {
            LOGGER.error("Unexpected error initializing FreeType", t);
            available = false;
        }
    }

    /**
     * Load a font face from a direct ByteBuffer.
     * The buffer must remain valid for the lifetime of the face.
     *
     * @return FT_Face struct wrapper
     */
    public synchronized FT_Face loadFace(ByteBuffer fontData) {
        if (!available) {
            throw new IllegalStateException("FreeType is not available");
        }
        if (!fontData.isDirect()) {
            throw new IllegalArgumentException("Font data must be a direct ByteBuffer");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pFace = stack.mallocPointer(1);
            int error = FT_New_Memory_Face(library, fontData, 0, pFace);
            if (error != 0) {
                throw new RuntimeException("FT_New_Memory_Face failed with error: " + error);
            }
            return FT_Face.create(pFace.get(0));
        }
    }

    /**
     * Get the glyph index for a Unicode codepoint.
     */
    public synchronized int getCharIndex(FT_Face face, int codepoint) {
        return (int) FT_Get_Char_Index(face, codepoint);
    }

    /**
     * Extract the outline for a glyph. Returns null for empty/space glyphs.
     * <p>
     * Loads the glyph with {@code FT_LOAD_NO_BITMAP | FT_LOAD_NO_SCALE} to get
     * the raw outline in font units without hinting or scaling.
     */
    public synchronized GlyphOutline extractOutline(FT_Face face, int glyphIndex) {
        int error = FT_Load_Glyph(face, glyphIndex, FT_LOAD_NO_BITMAP | FT_LOAD_NO_SCALE);
        if (error != 0) {
            LOGGER.warn("FT_Load_Glyph failed for index {}: error {}", glyphIndex, error);
            return null;
        }

        FT_GlyphSlot slot = face.glyph();
        if (slot == null) {
            return null;
        }

        FT_Outline outline = slot.outline();
        if (outline.n_points() == 0) {
            return null;
        }

        int flags = outline.flags();
        // FT_OUTLINE_EVEN_ODD_FILL = 0x2: CFF/OTF fonts use even-odd fill rule,
        // TrueType fonts use non-zero winding. FreeType sets this flag accordingly.
        boolean evenOddFill = (flags & 0x2) != 0;
        // FT_OUTLINE_REVERSE_FILL = 0x4: set when outer contours are CCW (PostScript/CFF).
        // When NOT set, outer contours are CW (TrueType convention).
        boolean reverseFill = (flags & 0x4) != 0;

        GlyphOutline.Builder builder = new GlyphOutline.Builder();
        builder.setEvenOddFill(evenOddFill);
        builder.setReverseFill(reverseFill);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FT_Outline_Funcs funcs = FT_Outline_Funcs.malloc(stack);
            funcs.move_to((toPtr, user) -> {
                FT_Vector to = FT_Vector.create(toPtr);
                builder.moveTo(to.x(), to.y());
                return 0;
            });
            funcs.line_to((toPtr, user) -> {
                FT_Vector to = FT_Vector.create(toPtr);
                builder.lineTo(to.x(), to.y());
                return 0;
            });
            funcs.conic_to((controlPtr, toPtr, user) -> {
                FT_Vector control = FT_Vector.create(controlPtr);
                FT_Vector to = FT_Vector.create(toPtr);
                builder.conicTo(control.x(), control.y(), to.x(), to.y());
                return 0;
            });
            funcs.cubic_to((control1Ptr, control2Ptr, toPtr, user) -> {
                FT_Vector control1 = FT_Vector.create(control1Ptr);
                FT_Vector control2 = FT_Vector.create(control2Ptr);
                FT_Vector to = FT_Vector.create(toPtr);
                builder.cubicTo(control1.x(), control1.y(),
                        control2.x(), control2.y(),
                        to.x(), to.y());
                return 0;
            });
            funcs.shift(0);
            funcs.delta(0);

            error = FT_Outline_Decompose(outline, funcs, MemoryUtil.NULL);
            if (error != 0) {
                LOGGER.warn("FT_Outline_Decompose failed for index {}: error {}", glyphIndex, error);
                return null;
            }
        }

        return builder.build();
    }

    /**
     * Get glyph advance width in font units.
     */
    public synchronized long getGlyphAdvance(FT_Face face, int glyphIndex) {
        int error = FT_Load_Glyph(face, glyphIndex, FT_LOAD_NO_BITMAP | FT_LOAD_NO_SCALE);
        if (error != 0) {
            return 0;
        }
        FT_GlyphSlot slot = face.glyph();
        return slot != null ? slot.advance().x() : 0;
    }

    /**
     * Close a font face.
     */
    public synchronized void closeFace(FT_Face face) {
        FT_Done_Face(face);
    }

    /**
     * Shut down FreeType. Called on mod unload.
     */
    public synchronized void shutdown() {
        if (available) {
            FT_Done_FreeType(library);
            library = 0;
            available = false;
            initialized = false;
            LOGGER.info("FreeType library shut down");
        }
    }
}
