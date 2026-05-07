package net.tysontheember.emberstextapi.sdf;

public record PreBakedMSDF(
        byte[] msdfData,
        int texW,
        int texH,
        float bearingLeft,
        float bearingTop,
        float oversample
) {}
