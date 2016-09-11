package igrek.songbook.logic.controller.services;

import java.util.HashMap;
import java.util.Map;

import igrek.songbook.logger.Logs;

public class ServicesRegistry {

    private Map<String, IService> services;

    public ServicesRegistry() {
        services = new HashMap<>();
    }

    public <T extends IService> void registerService(T service) {
        String className = service.getClass().getName();
        boolean overwritten = services.get(className) != null;
        services.put(className, service);
        if (!overwritten) {
            Logs.debug("Service " + className + " registered");
        } else {
            Logs.debug("Service " + className + " registered (overwritten)");
        }
    }

    public <T extends IService> T getService(Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked")
            T service = (T) services.get(clazz.getName());
            return service;
        } catch (ClassCastException e) {
            Logs.error("Error while casting service " + clazz.getName());
            return null;
        } catch (NullPointerException e) {
            Logs.error("Service " + clazz.getName() + " was not found");
            return null;
        }
    }
}
