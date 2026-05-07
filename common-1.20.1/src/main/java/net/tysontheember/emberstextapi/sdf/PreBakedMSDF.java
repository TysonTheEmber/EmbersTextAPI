package net.tysontheember.emberstextapi.sdf;

public record PreBakedMSDF(
        byte[] msdfData,
        int texW,
        int texH,
        float bearingX,
        float bearingY,
        float oversample
) {}
