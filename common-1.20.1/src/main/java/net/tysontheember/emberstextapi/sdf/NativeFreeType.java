package net.tysontheember.emberstextapi.sdf;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.*;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Outline;
import org.lwjgl.util.freetype.FT_Outline_Funcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Compatibility layer for calling FreeType functions on MC 1.20.1.
 * <p>
 * LWJGL's {@code org.lwjgl.util.freetype.FreeType} class cannot be used on MC 1.20.1
 * because its static initializer references {@code Configuration.FREETYPE_LIBRARY_NAME},
 * a field that only exists in LWJGL core 3.3.2+. MC 1.20.1 ships LWJGL 3.3.1.
 * <p>
 * This class loads the FreeType native library directly using LWJGL 3.3.1's
 * {@link Library} API and calls functions via {@link JNI} by address, bypassing
 * the problematic {@code FreeType} wrapper class entirely.
 * <p>
 * The struct wrapper classes ({@link FT_Face}, {@link FT_Outline}, etc.) are used
 * directly since they extend {@code Struct} which is binary-compatible after generic erasure.
 */
public final class NativeFreeType {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/NativeFreeType");

    // FreeType load flags
    public static final int FT_LOAD_DEFAULT = 0;
    public static final int FT_LOAD_NO_SCALE = 1 << 0;
    public static final int FT_LOAD_NO_BITMAP = 1 << 3;

    private static SharedLibrary LIBRARY;
    private static boolean initAttempted;
    private static boolean available;

    // Function addresses
    private static long FT_Init_FreeType;
    private static long FT_Done_FreeType;
    private static long FT_New_Memory_Face;
    private static long FT_Done_Face;
    private static long FT_Get_Char_Index;
    private static long FT_Load_Glyph;
    private static long FT_Outline_Decompose;
    private static long FT_Get_First_Char;
    private static long FT_Get_Next_Char;

    private NativeFreeType() {}

    /**
     * Initialize the native FreeType library. Safe to call multiple times.
     * @return true if FreeType is available
     */
    public static synchronized boolean init() {
        if (initAttempted) return available;
        initAttempted = true;

        try {
            // Load the FreeType shared library using LWJGL 3.3.1's API
            // This tries: system library path, then extracts from natives JAR on classpath
            LIBRARY = Library.loadNative(NativeFreeType.class, "org.lwjgl.freetype",
                    (Configuration<String>) null, "freetype", "libfreetype");

            // Resolve all function addresses
            FT_Init_FreeType = func("FT_Init_FreeType");
            FT_Done_FreeType = func("FT_Done_FreeType");
            FT_New_Memory_Face = func("FT_New_Memory_Face");
            FT_Done_Face = func("FT_Done_Face");
            FT_Get_Char_Index = func("FT_Get_Char_Index");
            FT_Load_Glyph = func("FT_Load_Glyph");
            FT_Outline_Decompose = func("FT_Outline_Decompose");
            FT_Get_First_Char = func("FT_Get_First_Char");
            FT_Get_Next_Char = func("FT_Get_Next_Char");

            available = true;
            LOGGER.info("FreeType native library loaded successfully via compatibility layer");
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            LOGGER.warn("FreeType native library not available: {}", e.getMessage());
            available = false;
        } catch (Throwable t) {
            LOGGER.error("Unexpected error loading FreeType native library", t);
            available = false;
        }

        return available;
    }

    public static boolean isAvailable() {
        return available;
    }

    private static long func(String name) {
        long addr = LIBRARY.getFunctionAddress(name);
        if (addr == 0) throw new UnsatisfiedLinkError("FreeType function not found: " + name);
        return addr;
    }

    // --- Function wrappers ---
    // JNI naming: P=pointer(long), I=int, J=java-long. On 64-bit, P/J/N are interchangeable.
    // FreeType's FT_Long/FT_ULong are C long = 8 bytes on 64-bit = same as pointer.

    /** FT_Init_FreeType(FT_Library*) -> FT_Error */
    public static int FT_Init_FreeType(PointerBuffer alibrary) {
        return JNI.invokePI(alibrary.address(), FT_Init_FreeType);
    }

    /** FT_Done_FreeType(FT_Library) -> FT_Error */
    public static int FT_Done_FreeType(long library) {
        return JNI.invokePI(library, FT_Done_FreeType);
    }

    /** FT_New_Memory_Face(FT_Library, const FT_Byte*, FT_Long, FT_Long, FT_Face*) -> FT_Error */
    public static int FT_New_Memory_Face(long library, ByteBuffer fileBase, long faceIndex, PointerBuffer aface) {
        return JNI.invokePPPPPI(library, MemoryUtil.memAddress(fileBase),
                (long) fileBase.remaining(), faceIndex, aface.address(), FT_New_Memory_Face);
    }

    /** FT_Done_Face(FT_Face) -> FT_Error */
    public static int FT_Done_Face(FT_Face face) {
        return JNI.invokePI(face.address(), FT_Done_Face);
    }

    /** FT_Get_Char_Index(FT_Face, FT_ULong) -> FT_UInt */
    public static int FT_Get_Char_Index(FT_Face face, int codepoint) {
        // FT_ULong charcode = pointer-sized, FT_UInt result = 32-bit
        // invokePPI: two pointer-sized params (face, charcode) + funcaddr -> returns int
        return JNI.invokePPI(face.address(), (long) codepoint, FT_Get_Char_Index);
    }

    /** FT_Load_Glyph(FT_Face, FT_UInt, FT_Int32) -> FT_Error */
    public static int FT_Load_Glyph(FT_Face face, int glyphIndex, int loadFlags) {
        return JNI.invokePI(face.address(), glyphIndex, loadFlags, FT_Load_Glyph);
    }

    /** FT_Outline_Decompose(FT_Outline*, FT_Outline_Funcs*, void*) -> FT_Error */
    public static int FT_Outline_Decompose(FT_Outline outline, FT_Outline_Funcs funcTable, long user) {
        return JNI.invokePPPI(outline.address(), funcTable.address(), user, FT_Outline_Decompose);
    }

    /** FT_Get_First_Char(FT_Face, FT_UInt*) -> FT_ULong */
    public static long FT_Get_First_Char(FT_Face face, IntBuffer agindex) {
        // Returns FT_ULong (pointer-sized). Use invokePPJ (ptr, ptr) -> long
        return JNI.invokePPJ(face.address(), MemoryUtil.memAddress(agindex), FT_Get_First_Char);
    }

    /** FT_Get_Next_Char(FT_Face, FT_ULong, FT_UInt*) -> FT_ULong */
    public static long FT_Get_Next_Char(FT_Face face, long charCode, IntBuffer agindex) {
        // Use invokePPPP (ptr, ptr-sized-charcode, ptr, funcaddr) -> long
        return JNI.invokePPPP(face.address(), charCode, MemoryUtil.memAddress(agindex), FT_Get_Next_Char);
    }

}
