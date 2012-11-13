package org.jboss.ddoyle.infinispan.demo.cache;

import org.infinispan.manager.CacheContainer;

public interface CacheContainerProvider {

	public abstract CacheContainer getCacheContainer();
	
	
	
}
