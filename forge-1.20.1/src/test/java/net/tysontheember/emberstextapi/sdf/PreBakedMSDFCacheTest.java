package net.tysontheember.emberstextapi.sdf;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class PreBakedMSDFCacheTest {

    private static PreBakedMSDFCache.CacheKey randomKey() {
        long h = ThreadLocalRandom.current().nextLong();
        return new PreBakedMSDFCache.CacheKey(h, 48, 4, 4.0f, 16.0f, 1.0f, 8.0f, 3.0f, 0f, 0f, "");
    }

    @Test
    void sameKeyReturnsSameInstance() {
        PreBakedMSDFCache.CacheKey key = randomKey();
        Map<Integer, PreBakedMSDF> a = PreBakedMSDFCache.getOrCreate(key);
        Map<Integer, PreBakedMSDF> b = PreBakedMSDFCache.getOrCreate(key);
        assertSame(a, b);
    }

    @Test
    void differentKeysReturnDifferentInstances() {
        Map<Integer, PreBakedMSDF> a = PreBakedMSDFCache.getOrCreate(randomKey());
        Map<Integer, PreBakedMSDF> b = PreBakedMSDFCache.getOrCreate(randomKey());
        assertNotSame(a, b);
    }

    @Test
    void writesPropagateToSharedMap() {
        PreBakedMSDFCache.CacheKey key = randomKey();
        Map<Integer, PreBakedMSDF> a = PreBakedMSDFCache.getOrCreate(key);
        PreBakedMSDF data = new PreBakedMSDF(new byte[]{0, 0, 0}, 1, 1, 0f, 0f, 1.0f);
        a.put(65, data);

        Map<Integer, PreBakedMSDF> b = PreBakedMSDFCache.getOrCreate(key);
        assertSame(data, b.get(65));
    }

    @Test
    void equalKeysWithSameValuesShareMap() {
        long h = ThreadLocalRandom.current().nextLong();
        PreBakedMSDFCache.CacheKey k1 = new PreBakedMSDFCache.CacheKey(h, 48, 4, 4.0f, 16.0f, 1.0f, 8.0f, 3.0f, 0f, 0f, "");
        PreBakedMSDFCache.CacheKey k2 = new PreBakedMSDFCache.CacheKey(h, 48, 4, 4.0f, 16.0f, 1.0f, 8.0f, 3.0f, 0f, 0f, "");
        assertEquals(k1, k2);
        assertSame(PreBakedMSDFCache.getOrCreate(k1), PreBakedMSDFCache.getOrCreate(k2));
    }
}
