package net.tysontheember.emberstextapi.duck;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.tysontheember.emberstextapi.client.text.SpanEffect;
import net.tysontheember.emberstextapi.client.text.TypewriterTrack;

public interface ETAStyle {
    List<SpanEffect> eta$getEffects();

    void eta$setEffects(List<SpanEffect> effects);

    @Nullable
    TypewriterTrack eta$getTrack();

    void eta$setTrack(@Nullable TypewriterTrack track);

    int eta$getTypewriterIndex();

    void eta$setTypewriterIndex(int index);

    float eta$getNeonIntensity();

    void eta$setNeonIntensity(float intensity);

    float eta$getWobbleAmplitude();

    void eta$setWobbleAmplitude(float amplitude);

    float eta$getWobbleSpeed();

    void eta$setWobbleSpeed(float speed);

    float eta$getGradientFlow();

    void eta$setGradientFlow(float flow);
}
