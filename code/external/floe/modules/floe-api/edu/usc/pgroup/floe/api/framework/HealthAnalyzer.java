package edu.usc.pgroup.floe.api.framework;

public abstract class HealthAnalyzer {
	
	
	javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    
    // This methods allows classes to register for MyEvents
    public void addHealthManagerEventListener(HealthManagerEventListener listener) {
        listenerList.add(HealthManagerEventListener.class, listener);
    }

    // This methods allows classes to unregister for MyEvents
    public void removeHealthManagerEventListener(HealthManagerEventListener listener) {
        listenerList.remove(HealthManagerEventListener.class, listener);
    }
    
    public void fireHealthManagerEvent(HealthManagerEvent evt) {
    	
    	//System.out.println("Firing event c:" + evt.getContainerID() + " f:" + evt.getFlakeID() + " a:" + evt.getSuggestedAction());
    	
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == HealthManagerEventListener.class) {
                ((HealthManagerEventListener)listeners[i+1]).CriticalEventOccurred(evt);
            }
        }
    }
    
}
