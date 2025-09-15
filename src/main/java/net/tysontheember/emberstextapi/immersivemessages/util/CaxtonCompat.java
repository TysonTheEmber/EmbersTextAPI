package net.tysontheember.emberstextapi.immersivemessages.util;

import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@OnlyIn(Dist.CLIENT)
public final class CaxtonCompat {
    private static final String MOD_ID = "caxton";
    private static final String RENDERER_CLASS = "xyz.flirora.caxton.render.CaxtonTextRenderer";
    private static final String HANDLER_METHOD = "getHandler";
    private static final String WIDTH_METHOD = "getWidth";
    private static final String HANDLER_CLASS = "xyz.flirora.caxton.layout.CaxtonTextHandler";

    private static volatile WidthProvider cachedHandler;
    private static volatile boolean attemptedLoad;

    private CaxtonCompat() {
    }

    public static WidthProvider getHandler() {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return null;
        }
        WidthProvider handler = cachedHandler;
        if (handler != null || attemptedLoad) {
            return handler;
        }
        synchronized (CaxtonCompat.class) {
            if (cachedHandler != null || attemptedLoad) {
                return cachedHandler;
            }
            cachedHandler = loadHandler();
            attemptedLoad = true;
            return cachedHandler;
        }
    }

    private static WidthProvider loadHandler() {
        try {
            Class<?> rendererClass = Class.forName(RENDERER_CLASS);
            Method getInstance = rendererClass.getDeclaredMethod("getInstance");
            Object renderer = getInstance.invoke(null);
            Method getHandler = rendererClass.getDeclaredMethod(HANDLER_METHOD);
            Object handlerInstance = getHandler.invoke(renderer);
            if (handlerInstance == null) {
                return null;
            }
            Class<?> handlerClass = Class.forName(HANDLER_CLASS);
            Method width = handlerClass.getMethod(WIDTH_METHOD, FormattedCharSequence.class);
            return sequence -> invokeWidth(handlerInstance, width, sequence);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError e) {
            return null;
        }
    }

    private static float invokeWidth(Object handler, Method width, FormattedCharSequence sequence) {
        try {
            Object result = width.invoke(handler, sequence);
            if (result instanceof Number number) {
                return number.floatValue();
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
        return Float.NaN;
    }

    @OnlyIn(Dist.CLIENT)
    @FunctionalInterface
    public interface WidthProvider {
        float getWidth(FormattedCharSequence sequence);
    }
}