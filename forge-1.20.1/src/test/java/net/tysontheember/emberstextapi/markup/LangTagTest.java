package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangTagTest {

    @BeforeAll
    static void stubResolver() {
        MarkupParser.setLangResolver((key, args) -> {
            if (key == null || key.isEmpty()) return "";
            return switch (key) {
                case "item.minecraft.diamond" -> "Diamond";
                case "test.greeting" -> "Hello, %s!";
                case "test.contains_markup" -> "<rainbow>NOPE</rainbow>";
                default -> key;
            };
        });
    }

    @AfterAll
    static void resetResolver() {
        MarkupParser.setLangResolver(null);
    }

    @Test
    void shorthandResolves() {
        List<TextSpan> spans = MarkupParser.parse("<lang:item.minecraft.diamond>");
        assertEquals(1, spans.size());
        assertEquals("Diamond", spans.get(0).getContent());
    }

    @Test
    void attributeFormResolves() {
        List<TextSpan> spans = MarkupParser.parse("<lang key=item.minecraft.diamond>");
        assertEquals(1, spans.size());
        assertEquals("Diamond", spans.get(0).getContent());
    }

    @Test
    void argsAreSubstituted() {
        MarkupParser.setLangResolver((key, args) -> {
            if ("test.greeting".equals(key) && args != null && args.length == 1) {
                return "Hello, " + args[0] + "!";
            }
            return key;
        });
        List<TextSpan> spans = MarkupParser.parse("<lang key=test.greeting args=Steve>");
        assertEquals(1, spans.size());
        assertEquals("Hello, Steve!", spans.get(0).getContent());
        stubResolver();
    }

    @Test
    void missingKeyRendersKeyLiterally() {
        List<TextSpan> spans = MarkupParser.parse("<lang:totally.missing.key>");
        assertEquals(1, spans.size());
        assertEquals("totally.missing.key", spans.get(0).getContent());
    }

    @Test
    void surroundingTagsStyleResolvedText() {
        List<TextSpan> spans = MarkupParser.parse("<bold><lang:item.minecraft.diamond></bold>");
        assertEquals(1, spans.size());
        assertEquals("Diamond", spans.get(0).getContent());
        assertEquals(Boolean.TRUE, spans.get(0).getBold());
    }

    @Test
    void resolvedTextIsNotReparsedForTags() {
        List<TextSpan> spans = MarkupParser.parse("<lang:test.contains_markup>");
        assertEquals(1, spans.size());
        assertEquals("<rainbow>NOPE</rainbow>", spans.get(0).getContent());
        assertTrue(spans.get(0).getEffects() == null || spans.get(0).getEffects().isEmpty(),
                "translation file content must not be re-parsed for ETA tags");
    }

    @Test
    void emptyTagYieldsEmptyText() {
        List<TextSpan> spans = MarkupParser.parse("before<lang>after");
        StringBuilder sb = new StringBuilder();
        for (TextSpan s : spans) sb.append(s.getContent());
        assertEquals("beforeafter", sb.toString());
    }

    @Test
    void multipleLangTagsResolveIndependently() {
        List<TextSpan> spans = MarkupParser.parse("<lang:item.minecraft.diamond> and <lang:item.minecraft.diamond>");
        StringBuilder sb = new StringBuilder();
        for (TextSpan s : spans) sb.append(s.getContent());
        assertEquals("Diamond and Diamond", sb.toString());
    }
}
