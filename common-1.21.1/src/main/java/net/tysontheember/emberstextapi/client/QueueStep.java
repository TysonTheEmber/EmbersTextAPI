package net.tysontheember.emberstextapi.client;

import java.util.List;

public record QueueStep(List<QueuedMessage> messages) {}
