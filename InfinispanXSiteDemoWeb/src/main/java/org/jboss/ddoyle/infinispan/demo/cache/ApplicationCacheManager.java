package org.jboss.ddoyle.infinispan.demo.cache;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.infinispan.Cache;

@ApplicationScoped
public class ApplicationCacheManager implements CacheManager {

	private static final String APPLICATION_DEFAULT_CACHE_NAME = "MyCoolCache";
	
	@Inject
	private CacheContainerProvider cacheManager;
	
	
	public Cache<String, Object> getCache() {
		return cacheManager.getCacheContainer().getCache(APPLICATION_DEFAULT_CACHE_NAME);
	}

	public Cache<String, Object> getCache(String cacheName) {
		return cacheManager.getCacheContainer().getCache(APPLICATION_DEFAULT_CACHE_NAME);
	}

	public void startCache() {
		getCache().start();
	}

	public void startCache(String cacheName) {
		getCache(cacheName).start();
		
	}

	public void stopCache() {
		getCache().stop();
	}

	public void stopCache(String cacheName) {
		getCache(cacheName).stop();
	}

	public void stopCacheContainer() {
		cacheManager.getCacheContainer().stop();
	}

}
