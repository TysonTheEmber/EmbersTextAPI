package net.tysontheember.emberstextapi.text;

/**
 * Factory responsible for compiling an {@link Attribute} definition into a
 * runtime {@link TextEffect} instance. Factories validate the supplied
 * {@link Params} using the {@link ParamSpec} definition and return a fully
 * configured effect ready for per-glyph evaluation.
 */
@FunctionalInterface
public interface TextAttributeFactory {
    /**
     * @return the identifier for the attribute handled by this factory.
     */
    default EmbersKey id() {
        throw new UnsupportedOperationException("Factory must override id()");
    }

    /**
     * @return the parameter specification used for validation. Implementations
     * should override when validation is required.
     */
    default ParamSpec spec() {
        return ParamSpec.builder().build();
    }

    /**
     * Compiles the supplied parameters into a {@link TextEffect}.
     *
     * @param params   validated parameters.
     * @param context  compile context containing layout information.
     * @return compiled effect ready for evaluation at render time.
     */
    TextEffect compile(Params params, TextEffect.CompileContext context);
}
