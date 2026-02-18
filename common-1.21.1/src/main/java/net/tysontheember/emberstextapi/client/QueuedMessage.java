package net.tysontheember.emberstextapi.client;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import java.util.UUID;

public record QueuedMessage(UUID id, ImmersiveMessage message) {}
