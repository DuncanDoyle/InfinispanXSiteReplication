package org.jboss.ddoyle.infinispan.demo.cache.info;

import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.ddoyle.infinispan.demo.cache.CacheManager;

public class LocalCacheInfoService implements CacheInfoService {

	@Inject
	private CacheManager cacheManager;
	
	@Override
	public int getCacheSize() {
		return cacheManager.getCache().size();
	}

	@Override
	public Set<Entry<String, Object>> getCacheEntries() {
		return cacheManager.getCache().entrySet();
	}
	

}
