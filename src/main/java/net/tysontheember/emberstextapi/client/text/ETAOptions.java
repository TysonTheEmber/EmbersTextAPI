package net.tysontheember.emberstextapi.client.text;

/**
 * Minimal client-side toggles used while computing typewriter progression.
 */
public record ETAOptions(TypewriterController.Mode typewriterMode, float typewriterSpeed) {
    public static final ETAOptions DEFAULT = new ETAOptions(TypewriterController.Mode.OFF, 1.0f);

    public ETAOptions {
        if (typewriterMode == null) {
            typewriterMode = TypewriterController.Mode.OFF;
        }
        if (!Float.isFinite(typewriterSpeed) || typewriterSpeed <= 0.0f) {
            typewriterSpeed = 1.0f;
        }
    }

    public boolean isTypewriterEnabled() {
        return this.typewriterMode != TypewriterController.Mode.OFF;
    }
}
