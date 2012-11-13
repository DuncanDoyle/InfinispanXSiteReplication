package org.jboss.ddoyle.infinispan.demo.cache;

import org.infinispan.Cache;

public interface CacheManager {
	
	public abstract Cache<String, Object> getCache();
	
	public abstract Cache<String, Object> getCache(final String cacheName);
	
	public abstract void startCache();
	
	public abstract void startCache(final String cacheName);
	
	public abstract void stopCache();
	
	public abstract void stopCache(final String cacheName);
	
	public abstract void stopCacheContainer();

}
