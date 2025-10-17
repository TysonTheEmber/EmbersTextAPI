package net.tysontheember.emberstextapi.inline;

import java.util.List;

/**
 * Factory for tag attributes created during parsing.
 */
@FunctionalInterface
public interface TagFactory {
    List<TagAttribute> create(TagToken token, TagParserContext context) throws TagParseException;
}
