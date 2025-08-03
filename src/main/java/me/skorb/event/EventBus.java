package me.skorb.event;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/*
* Acts as a central hub for event handling.
* Decouples UI components from controllers
* */
public class EventBus {
    private static final Map<Class<?>, Consumer<Object>> listeners = new HashMap<>();

    public static <T> void addListener(Class<T> eventType, Consumer<T> listener) {
        listeners.put(eventType, (Consumer<Object>) listener);
    }

    public static void fireEvent(Object event) {
        Consumer<Object> listener = listeners.get(event.getClass());
        if (listener != null) {
            listener.accept(event);
        }
    }
}
