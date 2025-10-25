package net.tysontheember.emberstextapi.mixin.client;

import java.util.Collections;
import java.util.List;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.client.text.SpanEffect;
import net.tysontheember.emberstextapi.client.text.SpanGraph;
import net.tysontheember.emberstextapi.client.text.SpanStyleExtras;
import net.tysontheember.emberstextapi.client.text.TypewriterTrack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Style.class)
public class StyleMixin implements SpanStyleExtras {
    @Unique
    private SpanGraph emberstextapi$spanGraph;

    @Unique
    private List<SpanEffect> emberstextapi$activeEffects = Collections.emptyList();

    @Unique
    private TypewriterTrack emberstextapi$typewriterTrack;

    @Unique
    private int emberstextapi$typewriterIndex;

    @Unique
    private String emberstextapi$spanSignature;

    @Override
    public SpanGraph eta$getSpanGraph() {
        return emberstextapi$spanGraph;
    }

    @Override
    public void eta$setSpanGraph(SpanGraph graph) {
        this.emberstextapi$spanGraph = graph;
    }

    @Override
    public List<SpanEffect> eta$getActiveEffects() {
        return emberstextapi$activeEffects;
    }

    @Override
    public void eta$setActiveEffects(List<SpanEffect> effects) {
        this.emberstextapi$activeEffects = effects == null ? Collections.emptyList() : effects;
    }

    @Override
    public TypewriterTrack eta$getTypewriterTrack() {
        return emberstextapi$typewriterTrack;
    }

    @Override
    public void eta$setTypewriterTrack(TypewriterTrack track) {
        this.emberstextapi$typewriterTrack = track;
    }

    @Override
    public int eta$getTypewriterIndex() {
        return emberstextapi$typewriterIndex;
    }

    @Override
    public void eta$setTypewriterIndex(int index) {
        this.emberstextapi$typewriterIndex = index;
    }

    @Override
    public String eta$getSpanSignature() {
        return emberstextapi$spanSignature;
    }

    @Override
    public void eta$setSpanSignature(String signature) {
        this.emberstextapi$spanSignature = signature;
    }
}
