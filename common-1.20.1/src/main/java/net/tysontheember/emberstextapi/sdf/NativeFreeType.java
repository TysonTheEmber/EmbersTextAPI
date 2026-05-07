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

public final class NativeFreeType {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/NativeFreeType");

    public static final int FT_LOAD_DEFAULT = 0;
    public static final int FT_LOAD_NO_SCALE = 1 << 0;
    public static final int FT_LOAD_NO_BITMAP = 1 << 3;

    private static SharedLibrary LIBRARY;
    private static boolean initAttempted;
    private static boolean available;

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

    public static synchronized boolean init() {
        if (initAttempted) return available;
        initAttempted = true;

        try {

            LIBRARY = Library.loadNative(NativeFreeType.class, "org.lwjgl.freetype",
                    (Configuration<String>) null, "freetype", "libfreetype");

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

    public static int FT_Init_FreeType(PointerBuffer alibrary) {
        return JNI.invokePI(alibrary.address(), FT_Init_FreeType);
    }

    public static int FT_Done_FreeType(long library) {
        return JNI.invokePI(library, FT_Done_FreeType);
    }

    public static int FT_New_Memory_Face(long library, ByteBuffer fileBase, long faceIndex, PointerBuffer aface) {
        return JNI.invokePPPPPI(library, MemoryUtil.memAddress(fileBase),
                (long) fileBase.remaining(), faceIndex, aface.address(), FT_New_Memory_Face);
    }

    public static int FT_Done_Face(FT_Face face) {
        return JNI.invokePI(face.address(), FT_Done_Face);
    }

    public static int FT_Get_Char_Index(FT_Face face, int codepoint) {

        return JNI.invokePPI(face.address(), (long) codepoint, FT_Get_Char_Index);
    }

    public static int FT_Load_Glyph(FT_Face face, int glyphIndex, int loadFlags) {
        return JNI.invokePI(face.address(), glyphIndex, loadFlags, FT_Load_Glyph);
    }

    public static int FT_Outline_Decompose(FT_Outline outline, FT_Outline_Funcs funcTable, long user) {
        return JNI.invokePPPI(outline.address(), funcTable.address(), user, FT_Outline_Decompose);
    }

    public static long FT_Get_First_Char(FT_Face face, IntBuffer agindex) {

        return JNI.invokePPJ(face.address(), MemoryUtil.memAddress(agindex), FT_Get_First_Char);
    }

    public static long FT_Get_Next_Char(FT_Face face, long charCode, IntBuffer agindex) {

        return JNI.invokePPPP(face.address(), charCode, MemoryUtil.memAddress(agindex), FT_Get_Next_Char);
    }

}
