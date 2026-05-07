package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.AlignAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.AnchorAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.BackgroundAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.FadeAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.OffsetAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.ScaleAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.ShadowAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.EmptyParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MessageAttributeTest {

    @BeforeAll
    static void initRegistries() {
        if (!net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry.isLocked()) {
            net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry.initializeDefaultEffects();
        }
        if (!MessageAttributeRegistry.isLocked()) {
            MessageAttributeRegistry.initializeDefaultAttributes();
        }
    }

    @Test
    void scalePositional() {
        var attr = MessageAttributeRegistry.parseTag("scale 2.5");
        assertInstanceOf(ScaleAttribute.class, attr);
        assertEquals("scale value=2.5", attr.serialize());
    }

    @Test
    void scaleExplicit() {
        var attr = MessageAttributeRegistry.parseTag("scale value=2.5");
        assertInstanceOf(ScaleAttribute.class, attr);
        assertEquals("scale value=2.5", attr.serialize());
    }

    @Test
    void scaleDefault() {
        var attr = MessageAttributeRegistry.create("scale", EmptyParams.INSTANCE);
        assertInstanceOf(ScaleAttribute.class, attr);
        assertEquals("scale value=1.0", attr.serialize());
    }

    @Test
    void offsetParsesXY() {
        var attr = (OffsetAttribute) MessageAttributeRegistry.parseTag("offset x=20 y=-10");
        assertEquals("offset x=20.0 y=-10.0", attr.serialize());
    }

    @Test
    void offsetDefault() {
        var attr = (OffsetAttribute) MessageAttributeRegistry.parseTag("offset");
        assertEquals("offset x=0.0 y=0.0", attr.serialize());
    }

    @Test
    void anchorPositional() {
        var attr = MessageAttributeRegistry.parseTag("anchor TOP_CENTER");
        assertEquals("anchor value=TOP_CENTER", attr.serialize());
    }

    @Test
    void anchorCaseInsensitive() {
        var attr = MessageAttributeRegistry.parseTag("anchor top_center");
        assertEquals("anchor value=TOP_CENTER", attr.serialize());
    }

    @Test
    void alignPositional() {
        var attr = MessageAttributeRegistry.parseTag("align LEFT");
        assertEquals("align value=LEFT", attr.serialize());
    }

    @Test
    void shadowBareDefaultsTrue() {
        var attr = (ShadowAttribute) MessageAttributeRegistry.parseTag("shadow");
        assertEquals("shadow value=true", attr.serialize());
    }

    @Test
    void shadowFalse() {
        var attr = (ShadowAttribute) MessageAttributeRegistry.parseTag("shadow false");
        assertEquals("shadow value=false", attr.serialize());
    }

    @Test
    void fadeInOnly() {
        var attr = (FadeAttribute) MessageAttributeRegistry.parseTag("fade in=10");
        assertEquals("fade in=10", attr.serialize());
    }

    @Test
    void fadeBoth() {
        var attr = (FadeAttribute) MessageAttributeRegistry.parseTag("fade in=10 out=20");
        assertEquals("fade in=10 out=20", attr.serialize());
    }

    @Test
    void bgEmptyEnablesBackground() {
        var attr = (BackgroundAttribute) MessageAttributeRegistry.parseTag("bg");
        assertEquals("bg", attr.serialize());
    }

    @Test
    void bgWithColor() {
        var attr = (BackgroundAttribute) MessageAttributeRegistry.parseTag("bg color=red");
        assertEquals("bg color=red", attr.serialize());
    }

    @Test
    void bgWithBorderPair() {
        var attr = (BackgroundAttribute) MessageAttributeRegistry.parseTag("bg color=red borderstart=blue borderend=green");
        assertEquals("bg color=red borderstart=blue borderend=green", attr.serialize());
    }

    @Test
    void bgWithFromTo() {
        var attr = (BackgroundAttribute) MessageAttributeRegistry.parseTag("bg from=red to=blue");
        assertEquals("bg from=red to=blue", attr.serialize());
    }

    @Test
    void bgGradientShorthand() {
        var attr = (BackgroundAttribute) MessageAttributeRegistry.parseTag("bg gradient=\"red,blue\"");
        assertEquals("bg from=red to=blue", attr.serialize());
    }

    @Test
    void backgroundAliasResolves() {
        var attr = MessageAttributeRegistry.parseTag("background color=red");
        assertInstanceOf(BackgroundAttribute.class, attr);
        assertEquals("bg color=red", attr.serialize(), "background alias canonicalizes to bg in serialize");
    }

    @Test
    void anchorDefault() {
        var attr = MessageAttributeRegistry.create("anchor", EmptyParams.INSTANCE);
        assertEquals("anchor value=TOP_CENTER", attr.serialize());
    }

    @Test
    void alignDefault() {
        var attr = MessageAttributeRegistry.create("align", EmptyParams.INSTANCE);
        assertEquals("align value=LEFT", attr.serialize());
    }

    @Test
    void parseFullExtractsScale() {
        var result = MarkupParser.parseFull("[scale 2]Hello");
        assertEquals(1, result.messageAttributes().size());
        assertEquals("scale value=2.0", result.messageAttributes().get(0).serialize());
        assertEquals(0, result.messageEffects().size());
        assertEquals("Hello", result.strippedMarkup());
    }

    @Test
    void parseFullExtractsMixed() {
        var result = MarkupParser.parseFull("[rock][bg color=red]Hello");
        assertEquals(1, result.messageEffects().size());
        assertEquals(1, result.messageAttributes().size());
        assertEquals("Hello", result.strippedMarkup());
    }

    @Test
    void parseFullUnknownBracketStaysLiteral() {
        var result = MarkupParser.parseFull("[unknown]Plain text");
        assertEquals(0, result.messageAttributes().size());
        assertEquals(0, result.messageEffects().size());
        assertEquals("[unknown]Plain text", result.strippedMarkup());
    }
}
