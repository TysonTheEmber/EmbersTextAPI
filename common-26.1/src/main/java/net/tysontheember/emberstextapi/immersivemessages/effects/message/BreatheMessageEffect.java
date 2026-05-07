package net.tysontheember.emberstextapi.immersivemessages.effects.message;

import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public class BreatheMessageEffect extends BaseMessageEffect {

    private final float amount;
    private final float speed;

    public BreatheMessageEffect(@NotNull Params params) {
        super(params);
        float rawAmount = params.getDouble("amount").map(Number::floatValue).orElse(0.05f);
        this.amount = Mth.clamp(rawAmount, 0f, 0.5f);
        this.speed = params.getDouble("speed").map(Number::floatValue).orElse(0.5f);
    }

    @Override
    public void apply(@NotNull MessageEffectContext ctx) {
        float t = ctx.elapsedSeconds + ctx.partialTick / 20f;
        float scale = 1f + amount * (float) Math.sin(Mth.TWO_PI * speed * t);
        float cx = ctx.messageWidth * 0.5f;
        float cy = ctx.messageHeight * 0.5f;
        ctx.graphics.pose().translate(cx, cy);
        ctx.graphics.pose().scale(scale, scale);
        ctx.graphics.pose().translate(-cx, -cy);
    }

    @NotNull
    @Override
    public String getName() {
        return "breathe";
    }
}
