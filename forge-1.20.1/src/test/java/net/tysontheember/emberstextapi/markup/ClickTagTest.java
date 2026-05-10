package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClickTagTest {

    @Test
    void linkShorthandIsOpenUrl() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click link=\"https://example.com\">site</click>");
        assertEquals(1, spans.size());
        assertEquals("site", spans.get(0).getContent());
        assertEquals("open_url", spans.get(0).getClickAction());
        assertEquals("https://example.com", spans.get(0).getClickValue());
    }

    @Test
    void fullFormOpenUrl() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"open_url\" value=\"https://example.com\">site</click>");
        assertEquals(1, spans.size());
        assertEquals("open_url", spans.get(0).getClickAction());
        assertEquals("https://example.com", spans.get(0).getClickValue());
    }

    @Test
    void runCommand() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"run_command\" value=\"/say hi\">go</click>");
        assertEquals("run_command", spans.get(0).getClickAction());
        assertEquals("/say hi", spans.get(0).getClickValue());
    }

    @Test
    void suggestCommand() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"suggest_command\" value=\"/foo \">go</click>");
        assertEquals("suggest_command", spans.get(0).getClickAction());
        assertEquals("/foo ", spans.get(0).getClickValue());
    }

    @Test
    void copyAliasResolvesToCopyToClipboard() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"copy\" value=\"abc\">x</click>");
        assertEquals("copy_to_clipboard", spans.get(0).getClickAction());
        assertEquals("abc", spans.get(0).getClickValue());
    }

    @Test
    void copyToClipboardCanonical() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"copy_to_clipboard\" value=\"abc\">x</click>");
        assertEquals("copy_to_clipboard", spans.get(0).getClickAction());
        assertEquals("abc", spans.get(0).getClickValue());
    }

    @Test
    void changePage() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"change_page\" value=\"3\">x</click>");
        assertEquals("change_page", spans.get(0).getClickAction());
        assertEquals("3", spans.get(0).getClickValue());
    }

    @Test
    void unknownActionLeavesNoClick() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"bogus\" value=\"x\">label</click>");
        assertEquals("label", spans.get(0).getContent());
        assertNull(spans.get(0).getClickAction());
        assertNull(spans.get(0).getClickValue());
    }

    @Test
    void missingValueLeavesNoClick() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click action=\"open_url\">label</click>");
        assertEquals("label", spans.get(0).getContent());
        assertNull(spans.get(0).getClickAction());
    }

    @Test
    void emptyClickIsInert() {
        List<TextSpan> spans = MarkupParser.parse("<click>label</click>");
        assertEquals(1, spans.size());
        assertEquals("label", spans.get(0).getContent());
        assertNull(spans.get(0).getClickAction());
    }

    @Test
    void actionWinsOverLink() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click link=\"https://from-link\" action=\"run_command\" value=\"/cmd\">x</click>");
        assertEquals("run_command", spans.get(0).getClickAction());
        assertEquals("/cmd", spans.get(0).getClickValue());
    }

    @Test
    void styleNestedInsideClickInheritsClick() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click link=\"https://example.com\"><b>bold site</b></click>");
        assertEquals(1, spans.size());
        assertEquals("bold site", spans.get(0).getContent());
        assertEquals(Boolean.TRUE, spans.get(0).getBold());
        assertEquals("open_url", spans.get(0).getClickAction());
        assertEquals("https://example.com", spans.get(0).getClickValue());
    }

    @Test
    void nestedClickOverrides() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click link=\"https://outer\">a"
                        + "<click action=\"run_command\" value=\"/inner\">b</click>"
                        + "c</click>");
        assertEquals("a", spans.get(0).getContent());
        assertEquals("open_url", spans.get(0).getClickAction());
        assertEquals("https://outer", spans.get(0).getClickValue());

        assertEquals("b", spans.get(1).getContent());
        assertEquals("run_command", spans.get(1).getClickAction());
        assertEquals("/inner", spans.get(1).getClickValue());

        assertEquals("c", spans.get(2).getContent());
        assertEquals("open_url", spans.get(2).getClickAction());
        assertEquals("https://outer", spans.get(2).getClickValue());
    }
}
