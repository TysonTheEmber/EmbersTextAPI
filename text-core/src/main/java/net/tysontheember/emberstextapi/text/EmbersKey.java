package net.tysontheember.emberstextapi.text;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

/**
 * Lightweight identifier used by the core text runtime. Mirrors the behaviour
 * of Minecraft's {@code ResourceLocation} without requiring a dependency on
 * game classes.
 */
public final class EmbersKey {
    public static final String DEFAULT_NAMESPACE = "embers";

    private final String namespace;
    private final String path;

    private EmbersKey(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public static EmbersKey of(String namespace, String path) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");
        String ns = normalize(namespace);
        String p = normalize(path);
        if (ns.isEmpty()) {
            throw new IllegalArgumentException("Namespace may not be empty");
        }
        if (p.isEmpty()) {
            throw new IllegalArgumentException("Path may not be empty");
        }
        return new EmbersKey(ns, p);
    }

    public static EmbersKey parse(String value) {
        Objects.requireNonNull(value, "value");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Identifier may not be blank");
        }
        int colon = trimmed.indexOf(':');
        if (colon >= 0) {
            String ns = trimmed.substring(0, colon);
            String path = trimmed.substring(colon + 1);
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Identifier missing path: " + value);
            }
            return of(ns, path);
        }
        return of(DEFAULT_NAMESPACE, trimmed);
    }

    private static String normalize(String token) {
        return token.trim().toLowerCase(Locale.ROOT);
    }

    @NotNull
    public String namespace() {
        return namespace;
    }

    @NotNull
    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmbersKey key)) return false;
        return namespace.equals(key.namespace) && path.equals(key.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
}
