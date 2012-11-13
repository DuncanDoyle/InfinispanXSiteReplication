package org.jboss.ddoyle.infinispan.demo.web.listener;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.ddoyle.infinispan.demo.cache.CacheManager;

/**
 * Application Lifecycle Listener implementation class CacheStartListener
 * 
 */
public class CacheStartListener implements ServletContextListener {

	@Inject
	private CacheManager cacheManager;
	
	/**
	 * Default constructor.
	 */
	public CacheStartListener() {
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent scEvent) {
		
		boolean startCachesOnStartup = Boolean.parseBoolean(scEvent.getServletContext().getInitParameter("startCachesOnStartup"));
		if (startCachesOnStartup == true) {
			cacheManager.startCache();
		}
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		//Stop the entire CacheContainer as the CacheContainer is managed by the application.
		cacheManager.stopCacheContainer();
	}

}
