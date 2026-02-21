* Added `/eta stopqueue <player> [channel]` — like clearqueue but also immediately closes the currently-displaying message(s) for that channel
* Added `/eta closeall <player>` — closes all messages on screen and clears all queues immediately
* Changed `/eta clearqueue <player>` (no channel) to only clear pending steps, letting current messages finish (consistent with the per-channel behavior)