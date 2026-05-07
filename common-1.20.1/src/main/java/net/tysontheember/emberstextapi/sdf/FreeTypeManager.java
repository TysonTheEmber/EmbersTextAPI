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
import java.util.LinkedHashSet;
import java.util.Set;

public final class FreeTypeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/FreeType");
    private static final FreeTypeManager INSTANCE = new FreeTypeManager();

    private long library;
    private boolean available;
    private boolean initialized;
    private final Set<FT_Face> openFaces = new LinkedHashSet<>();

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
                int error = NativeFreeType.FT_Init_FreeType(pLibrary);
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

    public synchronized FT_Face loadFace(ByteBuffer fontData) {
        if (!available) {
            throw new IllegalStateException("FreeType is not available");
        }
        if (!fontData.isDirect()) {
            throw new IllegalArgumentException("Font data must be a direct ByteBuffer");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pFace = stack.mallocPointer(1);
            int error = NativeFreeType.FT_New_Memory_Face(library, fontData, 0, pFace);
            if (error != 0) {
                throw new RuntimeException("FT_New_Memory_Face failed with error: " + error);
            }
            FT_Face face = FT_Face.create(pFace.get(0));
            openFaces.add(face);
            return face;
        }
    }

    public synchronized int getCharIndex(FT_Face face, int codepoint) {
        return NativeFreeType.FT_Get_Char_Index(face, codepoint);
    }

    public synchronized GlyphOutline extractOutline(FT_Face face, int glyphIndex) {
        int error = NativeFreeType.FT_Load_Glyph(face, glyphIndex,
                NativeFreeType.FT_LOAD_NO_BITMAP | NativeFreeType.FT_LOAD_NO_SCALE);
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

        boolean evenOddFill = (flags & 0x2) != 0;

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

            try {
                error = NativeFreeType.FT_Outline_Decompose(outline, funcs, MemoryUtil.NULL);
                if (error != 0) {
                    LOGGER.warn("FT_Outline_Decompose failed for index {}: error {}", glyphIndex, error);
                    return null;
                }
            } finally {
                funcs.move_to().free();
                funcs.line_to().free();
                funcs.conic_to().free();
                funcs.cubic_to().free();
            }
        }

        return builder.build();
    }

    public synchronized long getGlyphAdvance(FT_Face face, int glyphIndex) {
        int error = NativeFreeType.FT_Load_Glyph(face, glyphIndex,
                NativeFreeType.FT_LOAD_NO_BITMAP | NativeFreeType.FT_LOAD_NO_SCALE);
        if (error != 0) {
            return 0;
        }
        FT_GlyphSlot slot = face.glyph();
        return slot != null ? slot.advance().x() : 0;
    }

    public synchronized void closeFace(FT_Face face) {
        openFaces.remove(face);
        NativeFreeType.FT_Done_Face(face);
    }

    public synchronized void shutdown() {
        if (available) {
            for (FT_Face face : openFaces) {
                NativeFreeType.FT_Done_Face(face);
            }
            openFaces.clear();
            NativeFreeType.FT_Done_FreeType(library);
            library = 0;
            available = false;
            initialized = false;
            LOGGER.info("FreeType library shut down");
        }
    }
}
