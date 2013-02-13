package edu.usc.pgroup.floe.api.framework;

import java.util.EventListener;

public interface HealthManagerEventListener extends EventListener{
	public void CriticalEventOccurred(HealthManagerEvent evt);
}
