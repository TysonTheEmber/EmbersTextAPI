# EmbersTextAPI

## Shake Tags

Use the following tags in custom message data to enable shaking animations:

- `shakeWave`: Sinusoidal vertical wave.
- `shakeCircle`: Circular motion around the origin.
- `shakeRandom`: Random jitter each frame.

Per-character variants are also available using `charShakeWave`, `charShakeCircle`, and `charShakeRandom`.

Example command:

`/emberstextapi sendcustom @p {shakeWave:2.0f,charShakeRandom:2.0f} 100 "The ground trembles!"`

The old tag names (`wave`, `circle`, `random`, `waveChar`, `circleChar`, `randomChar`) are still accepted but deprecated and will trigger a log warning.
