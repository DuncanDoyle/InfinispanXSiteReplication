package org.jboss.ddoyle.infinispan.demo.web.rs;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Variant;

import org.codehaus.jackson.map.ObjectMapper;
import org.infinispan.Cache;
import org.infinispan.commons.hash.MurmurHash3;
import org.infinispan.remoting.MIMECacheEntry;
import org.jboss.ddoyle.infinispan.demo.cache.CacheManager;

import com.thoughtworks.xstream.XStream;

/**
 * Simple RESTful Infinispan Cache interface based on the JBoss DataGrid REST server.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@Path("/rest")
public class CacheRSService {

	private MurmurHash3 hashFunction = new MurmurHash3();

	private List<Variant> variantList = Variant.VariantListBuilder.newInstance()
			.mediaTypes(MediaType.APPLICATION_XML_TYPE, MediaType.APPLICATION_JSON_TYPE).build();

	private XStream xStream = new XStream();

	private ObjectMapper jsonMapper = new ObjectMapper();

	@Inject
	private CacheManager cacheManager;

	@GET
	@Path("/{cacheKey}")
	public Response get(@Context Request request, @PathParam("cacheKey") String key) {
		Response response;
		Cache cache = cacheManager.getCache();
		final Object entry = cache.get(key);
		if (entry != null) {
			// Determine the datatype we're going to return.
			if (entry instanceof MIMECacheEntry) {
				MIMECacheEntry mcEntry = (MIMECacheEntry) entry;
				Date lastModified = new Date(mcEntry.lastModified);

				ResponseBuilder builder = request.evaluatePreconditions(lastModified, getEntityTag(mcEntry));

				if (builder != null) {
					response = builder.build();
				} else {
					response = Response.ok(mcEntry.data, mcEntry.contentType).lastModified(lastModified).tag(getEntityTag(mcEntry)).build();
				}
			} else if (entry instanceof String) {
				response = Response.ok(entry, "text/plain").build();

			} else {
				// TODO: Directly copied from Infinispan Scala REST server. Clean up code.
				Variant variant = request.selectVariant(variantList);
				String selectMediaType;
				if (variant != null) {
					selectMediaType = variant.getMediaType().toString();
				} else {
					selectMediaType = "application/x-java-serialized-object";
				}

				if (selectMediaType.equals(MediaType.APPLICATION_JSON)) {
					response = Response.ok().type(selectMediaType).entity(new StreamingOutput() {
						@Override
						public void write(OutputStream output) throws IOException, WebApplicationException {
							jsonMapper.writeValue(output, entry);
						}
					}).build();

				} else if (selectMediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
					response = Response.ok().type(selectMediaType).entity(new StreamingOutput() {
						@Override
						public void write(OutputStream output) throws IOException, WebApplicationException {
							xStream.toXML(entry, output);
						}
					}).build();
				} else {
					// mediaType is "application/x-java-serialized-object"
					if (entry instanceof byte[]) {
						response = Response.ok().type("application/x-java-serialized-object").entity(new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								output.write((byte[]) entry);
							}
						}).build();
					} else if (entry instanceof Serializable) {
						response = Response.ok().type("application/x-java-serialized-object").entity(new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								new ObjectOutputStream(output).writeObject(entry);
							}
						}).build();

					} else {
						response = Response.notAcceptable(variantList).build();
					}
				}
			}
		} else {
			response = Response.status(Status.NOT_FOUND).build();
		}
		return response;

	}

	@PUT
	@POST
	@Path("/{cacheKey}")
	public Response putPost(@Context Request request, @PathParam("cacheKey") String key, @HeaderParam("Content-Type") String mediaType,
			byte[] data, @DefaultValue("-1") @HeaderParam("timeToLiveSecond") Long ttl,
			@DefaultValue("-1") @HeaderParam("maxIdleTimeSeconds") Long idleTime, @HeaderParam("performAsync") boolean useAsync) {
		Response response;
		Cache<String, Object> cache = cacheManager.getCache();
		if (request.getMethod() == HttpMethod.POST && cache.containsKey(key)) {
			response = Response.status(Status.CONFLICT).build();
		} else {
			Object entry = cache.get(key);
			if (entry instanceof MIMECacheEntry) {
				// Item exists, evaluate preconditions based on its attributes and headers.
				MIMECacheEntry mcEntry = (MIMECacheEntry) entry;
				Date lastModified = new Date(mcEntry.lastModified);
				ResponseBuilder builder = request.evaluatePreconditions(lastModified, getEntityTag(mcEntry));
				if (builder != null) {
					response = builder.build();
				} else {
					response = putInCache(mediaType, key, data, ttl, idleTime, useAsync);
				}
			} else {
				response = putInCache(mediaType, key, data, ttl, idleTime, useAsync);
			}
		}
		return response;
	}

	
	@DELETE
	@Path("/{cacheKey}")
	public Response delete(@Context Request request, @PathParam("cacheKey") String key,  @HeaderParam("performAsync") boolean useAsync) {
		Response response;
		
		Cache cache = cacheManager.getCache();
		Object entry = cache.get(key);
		if (entry == null) {
			response = Response.ok().build();
		} else if (entry instanceof MIMECacheEntry) {
			MIMECacheEntry mcEntry = (MIMECacheEntry) entry;
			Date lastModified = new Date(mcEntry.lastModified);
			ResponseBuilder builder = request.evaluatePreconditions(lastModified, getEntityTag(mcEntry));
			if  (builder != null) {
				//One of the pre-conditions failed, build a response.
				response = builder.build();
			} else {
				if (useAsync) {
					cache.removeAsync(key);
				} else {
					cache.remove(key);
				}
				response = Response.ok().build();
			}
		} else {
			if (useAsync) {
				cache.removeAsync(key);
			} else {
				cache.remove(key);
			}
			response = Response.ok().build();
		}
		
		return response;
	
	/*
		ManagerInstance.getEntry(cacheName, key) match {
        case b: MIMECacheEntry => {
           // The item exists in the cache, evaluate preconditions based on its attributes and the headers
	        val lastMod = new Date(b.lastModified)
	        request.evaluatePreconditions(lastMod, calcETAG(b)) match {
              // One of the preconditions failed, build a response
              case bldr: ResponseBuilder => bldr.build
              // Preconditions passed
              case null => {
                 if (useAsync) {
                    ManagerInstance.getCache(cacheName).removeAsync(key)
                 } else {
                    ManagerInstance.getCache(cacheName).remove(key)
                 }
                 Response.ok.build
              }
           }
        }
        case obj: Any => {
           if (useAsync) {
              ManagerInstance.getCache(cacheName).removeAsync(key)
           } else {
              ManagerInstance.getCache(cacheName).remove(key)
           }
           Response.ok.build
        }
        case null => Response.ok.build
        */
	}
	
	
	private Response putInCache(String mediaType, String key, byte[] data, Long ttl, Long idleTime, boolean async) {
		Cache<String, Object> cache = cacheManager.getCache();

		Object object;
		if (isBinaryType(mediaType)) {
			object = data;
		} else {
			object = new MIMECacheEntry(mediaType, data);
		}

		if (ttl != 0)
			if (idleTime == 0) {
				if (async) {
					cache.putAsync(key, object, ttl, TimeUnit.SECONDS);
				} else {
					cache.put(key, object, ttl, TimeUnit.SECONDS);
				}
			} else {
				if (async) {
					cache.putAsync(key, object, ttl, TimeUnit.SECONDS, idleTime, TimeUnit.SECONDS);
				} else {
					cache.put(key, object, ttl, TimeUnit.SECONDS, idleTime, TimeUnit.SECONDS);
				}
			}
		else {
			if (async) {
			cache.putAsync(key, object);
			} else {
				cache.put(key, object);
			}
		}
		return Response.ok().build();
	}

	private boolean isBinaryType(String mediaType) {
		return mediaType == "application/x-java-serialized-object";
	}

	private EntityTag getEntityTag(MIMECacheEntry entry) {
		return new EntityTag(entry.contentType + hashFunction.hash(entry.data));
	}

}
