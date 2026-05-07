package net.tysontheember.emberstextapi.immersivemessages.effects.message;

import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public class RockMessageEffect extends BaseMessageEffect {

    private final float angle;
    private final float speed;
    private final Pivot pivot;

    public RockMessageEffect(@NotNull Params params) {
        super(params);
        this.angle = params.getDouble("angle").map(Number::floatValue).orElse(5.0f);
        this.speed = params.getDouble("speed").map(Number::floatValue).orElse(1.0f);
        this.pivot = params.getString("pivot")
                .map(s -> switch (s.toLowerCase()) {
                    case "top" -> Pivot.TOP;
                    case "bottom" -> Pivot.BOTTOM;
                    default -> Pivot.CENTER;
                })
                .orElse(Pivot.CENTER);
    }

    @Override
    public void apply(@NotNull MessageEffectContext ctx) {
        float t = ctx.elapsedSeconds + ctx.partialTick / 20f;
        float theta = (float) Math.toRadians(angle * Math.sin(Mth.TWO_PI * speed * t));
        float px = ctx.messageWidth * 0.5f;
        float py = switch (pivot) {
            case TOP -> 0f;
            case CENTER -> ctx.messageHeight * 0.5f;
            case BOTTOM -> ctx.messageHeight;
        };
        ctx.graphics.pose().translate(px, py, 0);
        ctx.graphics.pose().mulPose(Axis.ZP.rotation(theta));
        ctx.graphics.pose().translate(-px, -py, 0);
    }

    @NotNull
    @Override
    public String getName() {
        return "rock";
    }
}
