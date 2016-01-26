package org.jboss.qa.ochaloup;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

public final class EjbCallUtils {
	private EjbCallUtils() {
		// can't be intialized
	}

	public static final String PROJECT_VERSION = "-1.0.0-SNAPSHOT";

	public static final String HOST = System.getProperty("call.host", "localhost");
	public static final int PORT = Integer.parseInt(System.getProperty("call.port", "8080"));
	public static final String SECURITY_USERNAME = System.getProperty("call.username", "guest");
	public static final String SECURITY_PASSWORD = System.getProperty("call.password", "guest");

	public static final <T> T lookup(final String earName, final String jarName, final Class<?> beanClass, final Class<T> remoteClass) {
		Properties props = new Properties();
		props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		props.put(Context.PROVIDER_URL, "http-remoting://" + HOST + ":" + PORT);
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		props.put(Context.SECURITY_PRINCIPAL, SECURITY_USERNAME);
		props.put(Context.SECURITY_CREDENTIALS, SECURITY_PASSWORD);
		// to avoid: java.lang.IllegalStateException: EJBCLIENT000025: No EJB receiver available
		props.put("jboss.naming.client.ejb.context", true);

		String beanLookup = null;
		Context context = null;
		try {
			context = new InitialContext(props);
			beanLookup = "ejb:" + earName + "/" + jarName + "/" + beanClass.getSimpleName() + "!" + remoteClass.getName();
	        // (isStateful ? "?stateful" : "");
			return remoteClass.cast(context.lookup(beanLookup));
		} catch (Exception e) {
			String err = String.format("Can't lookup for bean by lookup '%s' to '%s:%s' with credentials '%s/%s'",
					beanLookup, HOST, PORT, SECURITY_USERNAME, SECURITY_PASSWORD);
			throw new RuntimeException(err, e);
		} finally {
			if (context != null) {
				try {
					context.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

	}
}