package net.tysontheember.emberstextapi.ftb;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.tysontheember.emberstextapi.api.EmbersText;

public final class FtbQuestsBridge {
    private static final String MOD_ID = "ftbquests";

    private FtbQuestsBridge() {
    }

    public static Component maybeProcess(Component original, float timeSeconds) {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return original;
        }
        return EmbersText.render(original.getString(), timeSeconds);
    }
}
