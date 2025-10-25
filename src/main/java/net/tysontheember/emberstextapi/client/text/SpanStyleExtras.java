package net.tysontheember.emberstextapi.client.text;

import java.util.List;

/**
 * Bridge interface implemented by styles that carry EmbersTextAPI span extras.
 */
public interface SpanStyleExtras {
    SpanGraph eta$getSpanGraph();

    void eta$setSpanGraph(SpanGraph graph);

    List<SpanEffect> eta$getActiveEffects();

    void eta$setActiveEffects(List<SpanEffect> effects);

    TypewriterTrack eta$getTypewriterTrack();

    void eta$setTypewriterTrack(TypewriterTrack track);

    int eta$getTypewriterIndex();

    void eta$setTypewriterIndex(int index);

    String eta$getSpanSignature();

    void eta$setSpanSignature(String signature);
}
