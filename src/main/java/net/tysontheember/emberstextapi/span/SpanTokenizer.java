package net.tysontheember.emberstextapi.span;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class SpanTokenizer {
    private SpanTokenizer() {
    }

    static TagToken parse(String content, int offset) throws SpanParseException {
        if (content == null) {
            throw new SpanParseException("Empty tag", offset);
        }
        int length = content.length();
        int index = 0;
        boolean closing = false;
        boolean selfClosing = false;

        index = skipWhitespace(content, index, length);
        if (index < length && content.charAt(index) == '/') {
            closing = true;
            index++;
            index = skipWhitespace(content, index, length);
        }

        int nameStart = index;
        while (index < length) {
            char c = content.charAt(index);
            if (Character.isWhitespace(c) || c == '/' || c == '=') {
                break;
            }
            index++;
        }
        if (nameStart == index) {
            throw new SpanParseException("Missing tag name", offset + index);
        }
        String name = content.substring(nameStart, index).toLowerCase(Locale.ROOT);

        Map<String, String> attributes = new LinkedHashMap<>();

        index = skipWhitespace(content, index, length);

        if (!closing && index < length && content.charAt(index) == '=') {
            ParsedValue parsedValue = parseAttributeValue(content, offset, length, index + 1);
            attributes.put("value", SpanStrings.decodeEntities(parsedValue.value()));
            index = parsedValue.nextIndex();
        }

        while (index < length) {
            char c = content.charAt(index);
            if (c == '/') {
                selfClosing = true;
                index++;
                index = skipWhitespace(content, index, length);
                break;
            }
            if (closing) {
                throw new SpanParseException("Closing tag must not have attributes", offset + index);
            }
            int attrStart = index;
            while (index < length) {
                c = content.charAt(index);
                if (Character.isWhitespace(c) || c == '=' || c == '/') {
                    break;
                }
                index++;
            }
            if (attrStart == index) {
                throw new SpanParseException("Expected attribute name", offset + index);
            }
            String attrName = content.substring(attrStart, index).toLowerCase(Locale.ROOT);
            index = skipWhitespace(content, index, length);
            String attrValue = "true";
            if (index < length && content.charAt(index) == '=') {
                ParsedValue parsedValue = parseAttributeValue(content, offset, length, index + 1);
                attrValue = SpanStrings.decodeEntities(parsedValue.value());
                index = parsedValue.nextIndex();
            }
            attributes.put(attrName, attrValue);
        }

        if (index < length) {
            throw new SpanParseException("Unexpected characters in tag", offset + index);
        }

        return new TagToken(name, attributes, closing, selfClosing);
    }

    private static int skipWhitespace(String content, int index, int length) {
        while (index < length && Character.isWhitespace(content.charAt(index))) {
            index++;
        }
        return index;
    }

    private static ParsedValue parseAttributeValue(String content, int offset, int length, int index) throws SpanParseException {
        index = skipWhitespace(content, index, length);
        if (index >= length) {
            throw new SpanParseException("Missing attribute value", offset + index);
        }
        char delimiter = content.charAt(index);
        if (delimiter == '\'' || delimiter == '"') {
            int current = index + 1;
            StringBuilder builder = new StringBuilder();
            while (current < length) {
                char c = content.charAt(current);
                if (c == delimiter) {
                    return new ParsedValue(builder.toString(), skipWhitespace(content, current + 1, length));
                }
                if (c == '\\' && current + 1 < length) {
                    builder.append(content.charAt(current + 1));
                    current += 2;
                } else {
                    builder.append(c);
                    current++;
                }
            }
            throw new SpanParseException("Unterminated quoted attribute", offset + current);
        } else {
            int start = index;
            while (index < length) {
                char c = content.charAt(index);
                if (Character.isWhitespace(c) || c == '/') {
                    break;
                }
                index++;
            }
            return new ParsedValue(content.substring(start, index), skipWhitespace(content, index, length));
        }
    }

    private record ParsedValue(String value, int nextIndex) {
    }

    static final class TagToken {
        private final String name;
        private final Map<String, String> attributes;
        private final boolean closing;
        private final boolean selfClosing;

        TagToken(String name, Map<String, String> attributes, boolean closing, boolean selfClosing) {
            this.name = name;
            this.attributes = attributes;
            this.closing = closing;
            this.selfClosing = selfClosing;
        }

        String getName() {
            return name;
        }

        Map<String, String> getAttributes() {
            return attributes;
        }

        boolean isClosing() {
            return closing;
        }

        boolean isSelfClosing() {
            return selfClosing;
        }
    }
}
