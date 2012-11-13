package org.jboss.ddoyle.infinispan.demo.cache;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;

import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.util.Util;

/**
 * Provides a local CacheContainer in Infinispan 'Library-Mode'.
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@ApplicationScoped
public class LocalCacheContainerProvider implements CacheContainerProvider {

	private static final String INFINISPAN_CONFIG_FILE_NAME = "infinispan/infinispan.xml";
	
	private CacheContainer cacheManager;

	public synchronized CacheContainer getCacheContainer() {
		if (cacheManager == null) {
			// Retrieve Infinispan config file.
			InputStream infinispanConfigStream = this.getClass().getClassLoader().getResourceAsStream(INFINISPAN_CONFIG_FILE_NAME);
			try {
				try {
					cacheManager = new DefaultCacheManager(infinispanConfigStream);
				} catch (IOException ioe) {
					throw new RuntimeException("Error loading Infinispan CacheManager.", ioe);
				}
			} finally {
				// Use Infinispan Util class to flush and close stream.
				Util.close(infinispanConfigStream);
			}
		}
		return cacheManager;
	}

}
