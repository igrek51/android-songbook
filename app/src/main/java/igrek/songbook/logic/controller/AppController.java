package igrek.songbook.logic.controller;

import igrek.songbook.logic.controller.dispatcher.EventDispatcher;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.controller.services.IService;
import igrek.songbook.logic.controller.services.ServicesRegistry;

public class AppController {

    private ServicesRegistry servicesRegistry;

    private EventDispatcher eventDispatcher;

    private static AppController instance = null;

    /**
     * Reset instacji rejestru usług i wyczyszczenie listenerów eventów
     */
    public AppController() {
        servicesRegistry = new ServicesRegistry();
        eventDispatcher = new EventDispatcher();
        instance = this;
    }

    private static AppController getInstance() {
        if (instance == null) {
            new AppController();
        }
        return instance;
    }


    public static <T extends IService> void registerService(T service) {
        getInstance().servicesRegistry.registerService(service);
    }

    public static <T extends IService> T getService(Class<T> clazz) {
        return getInstance().servicesRegistry.getService(clazz);
    }

    public static void registerEventObserver(Class<? extends IEvent> eventClass, IEventObserver observer) {
        getInstance().eventDispatcher.registerEventObserver(eventClass, observer);
    }

    @Deprecated
    public static void clearEventObservers(Class<? extends IEvent> eventClass) {
        getInstance().eventDispatcher.clearEventObservers(eventClass);
    }

    public static void sendEvent(IEvent event) {
        getInstance().eventDispatcher.sendEvent(event);
    }
}
