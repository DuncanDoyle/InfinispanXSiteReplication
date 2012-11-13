package org.jboss.ddoyle.infinispan.demo.web.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infinispan.remoting.MIMECacheEntry;
import org.jboss.ddoyle.infinispan.demo.cache.info.CacheInfoService;

@WebServlet("/InfinispanInfo")
public class InfinispanInfoServlet extends HttpServlet {

	@Inject
	private CacheInfoService infoService;
	
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 12075602163409682L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Writer writer = resp.getWriter();
		writer.write("Infinispan Cache Info.\n\n\n");
		writer.write("Cache Size: " + infoService.getCacheSize() + "\n\n");
		
		writer.write("Cache Entries: \n");
		Set<Entry<String, Object>> entries = infoService.getCacheEntries();
		for (Entry<String,Object> nextEntry: entries) {
			String key = nextEntry.getKey();
			Object value = nextEntry.getValue();
			if (value instanceof MIMECacheEntry) {
				MIMECacheEntry mcValue = (MIMECacheEntry) value;
				String contentType = mcValue.contentType;
				//TODO conversion should be done based on the content-type.
				String mcData = new String (mcValue.data);
				writer.write("key: '" + nextEntry.getKey() + "', contentType: '" + contentType + "', value: '" + mcData + "'\n");
			} else {	
				writer.write("key: '" + nextEntry.getKey() + "', value: '" + nextEntry.getValue() + "'\n");
			}
		}
		
		writer.flush();
		writer.close();
	}
	
	
	

}
