package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.GlyphProvider;
import org.lwjgl.util.freetype.FT_Face;

import java.nio.ByteBuffer;

final class SDFGlyphProviderFactory {

    private SDFGlyphProviderFactory() {}

    static GlyphProvider create(ByteBuffer fontData, SDFConfig config) {
        FT_Face ftFace = FreeTypeManager.getInstance().loadFace(fontData);
        return new SDFGlyphProvider(ftFace, fontData, config);
    }
}
