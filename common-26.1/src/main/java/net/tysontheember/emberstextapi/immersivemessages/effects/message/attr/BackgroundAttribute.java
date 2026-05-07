package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackgroundAttribute implements MessageAttribute {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundAttribute.class);

    private final String colorRaw;
    private final String borderColorRaw;
    private final String borderStartRaw;
    private final String borderEndRaw;
    private final String fromRaw;
    private final String toRaw;

    public BackgroundAttribute(@NotNull Params params) {
        this.colorRaw       = params.getString("color").orElse(null);
        this.borderColorRaw = params.getString("bordercolor").orElse(null);
        this.borderStartRaw = params.getString("borderstart").orElse(null);
        this.borderEndRaw   = params.getString("borderend").orElse(null);

        String[] gradient = params.getString("gradient").map(this::splitGradient).orElse(null);
        if (gradient != null) {
            this.fromRaw = gradient[0];
            this.toRaw   = gradient[1];
        } else {
            this.fromRaw = params.getString("from").orElse(null);
            this.toRaw   = params.getString("to").orElse(null);
        }
    }

    private String[] splitGradient(String value) {
        String[] parts = value.split(",");
        if (parts.length != 2) {
            LOGGER.warn("Invalid bg gradient '{}': expected 'from,to'", value);
            return null;
        }
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    @Override
    public void apply(@NotNull ImmersiveMessage message) {
        if (fromRaw != null && toRaw != null) {
            ImmersiveColor f = ColorParser.parseImmersiveColor(fromRaw);
            ImmersiveColor t = ColorParser.parseImmersiveColor(toRaw);
            if (f != null && t != null) {
                message.backgroundGradient(f, t);
            } else {
                LOGGER.warn("Invalid bg gradient colors from='{}' to='{}'", fromRaw, toRaw);
            }
        } else if (colorRaw != null) {
            ImmersiveColor c = ColorParser.parseImmersiveColor(colorRaw);
            if (c != null) message.bgColor(c);
        } else {
            message.background(true);
        }

        if (borderStartRaw != null && borderEndRaw != null) {
            ImmersiveColor s = ColorParser.parseImmersiveColor(borderStartRaw);
            ImmersiveColor e = ColorParser.parseImmersiveColor(borderEndRaw);
            if (s != null && e != null) message.borderGradient(s, e);
        } else if (borderColorRaw != null) {
            ImmersiveColor c = ColorParser.parseImmersiveColor(borderColorRaw);
            if (c != null) message.borderGradient(c, c);
        }
    }

    @Override @NotNull public String getName() { return "bg"; }

    @Override
    @NotNull
    public String serialize() {
        StringBuilder sb = new StringBuilder("bg");
        if (fromRaw != null && toRaw != null) {
            sb.append(" from=").append(fromRaw).append(" to=").append(toRaw);
        } else if (colorRaw != null) {
            sb.append(" color=").append(colorRaw);
        }
        if (borderStartRaw != null && borderEndRaw != null) {
            sb.append(" borderstart=").append(borderStartRaw).append(" borderend=").append(borderEndRaw);
        } else if (borderColorRaw != null) {
            sb.append(" bordercolor=").append(borderColorRaw);
        }
        return sb.toString();
    }
}
