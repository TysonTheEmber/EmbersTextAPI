package net.tysontheember.emberstextapi.client.text;

import java.util.List;
import java.util.Optional;

/**
 * Registry placeholder for span effects.
 */
public final class SpanEffectRegistry {
    private SpanEffectRegistry() {
    }

    public static SpanEffectRegistry create() {
        return new SpanEffectRegistry();
    }

    public static Optional<SpanNode> findGradient(SpanGraph graph, int logicalIndex) {
        if (graph == null || graph.isEmpty()) {
            return Optional.empty();
        }
        SpanNode node = findGradient(graph.getRoots(), logicalIndex);
        return Optional.ofNullable(node);
    }

    public static Optional<SpanNode> findTypewriter(SpanGraph graph, int logicalIndex) {
        if (graph == null || graph.isEmpty()) {
            return Optional.empty();
        }
        SpanNode node = findTypewriter(graph.getRoots(), logicalIndex);
        return Optional.ofNullable(node);
    }

    private static SpanNode findGradient(List<SpanNode> nodes, int logicalIndex) {
        for (SpanNode node : nodes) {
            SpanNode result = findGradient(node, logicalIndex);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static SpanNode findGradient(SpanNode node, int logicalIndex) {
        if (logicalIndex < node.getStart() || logicalIndex >= node.getEnd()) {
            return null;
        }
        for (SpanNode child : node.getChildren()) {
            SpanNode match = findGradient(child, logicalIndex);
            if (match != null) {
                return match;
            }
        }
        return "grad".equals(node.getName()) ? node : null;
    }

    private static SpanNode findTypewriter(List<SpanNode> nodes, int logicalIndex) {
        for (SpanNode node : nodes) {
            SpanNode result = findTypewriter(node, logicalIndex);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static SpanNode findTypewriter(SpanNode node, int logicalIndex) {
        if (logicalIndex < node.getStart() || logicalIndex >= node.getEnd()) {
            return null;
        }
        for (SpanNode child : node.getChildren()) {
            SpanNode match = findTypewriter(child, logicalIndex);
            if (match != null) {
                return match;
            }
        }
        return "typewriter".equals(node.getName()) ? node : null;
    }
}
