package org.jboss.qa.ochaloup;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

public final class EjbCallUtils {
	private EjbCallUtils() {
		// can't be intialized
	}

	public static final String HOST = System.getProperty("call.host", "localhost");
	public static final String PORT = System.getProperty("call.port");
	// echo 'user=c5568adea472163dfc00c19c6348a665' >> standalone/configuration/application-users.properties
	public static final String SECURITY_USERNAME = System.getProperty("call.username", "user");
	public static final String SECURITY_PASSWORD = System.getProperty("call.password", "user");

	public static final <T> T lookup(final String earName, final String jarName, final Class<?> beanClass, final Class<T> remoteClass) {
		Properties props = new Properties();
		// props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		props.put(Context.PROVIDER_URL, "http-remoting://" + HOST + ":" + PORT);
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		props.put(Context.SECURITY_PRINCIPAL, SECURITY_USERNAME);
		props.put(Context.SECURITY_CREDENTIALS, SECURITY_PASSWORD);
		// to avoid: java.lang.IllegalStateException: EJBCLIENT000025: No EJB receiver available
		props.put("jboss.naming.client.ejb.context", true);

		String beanLookup = "ejb:" + earName + "/" + jarName + "/" + beanClass.getSimpleName() + "!" + remoteClass.getName();
		String lookupInfo = String.format("bean by lookup '%s' to '%s:%s' with credentials '%s/%s'",
				beanLookup, HOST, PORT, SECURITY_USERNAME, SECURITY_PASSWORD);

		Context context = null;
		try {
			context = new InitialContext(props);
			System.out.println("Looking at: " + lookupInfo);
			Thread.sleep(3 * 1000);
	        // (isStateful ? "?stateful" : "");
			return remoteClass.cast(context.lookup(beanLookup));
		} catch (Exception e) {
			throw new RuntimeException("Can't get: " + lookupInfo, e);
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

	/**
	 * Using the proprietary JBoss EJB Client API.
	 * See e.g. https://gist.github.com/jbandi/6287518
	 */
	public static final <T> T lookupProprietary(final String earName, final String jarName, final Class<?> beanClass, final Class<T> remoteClass) {
        final Properties ejbProperties = new Properties();
        ejbProperties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
        ejbProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        ejbProperties.put("remote.connections", "out");
        ejbProperties.put("remote.connection.out.host", HOST);
        ejbProperties.put("remote.connection.out.port", PORT);
        //ejbProperties.put("remote.connection.out.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER"); // needed for forcing authentication over remoting (i.e. if you have a custom login module)
        //ejbProperties.put("remote.connection.out.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false"); // needed for a login module that requires the password in plaintext
        ejbProperties.put("remote.connection.out.username", SECURITY_USERNAME);
        ejbProperties.put("remote.connection.out.password", SECURITY_PASSWORD);
        //ejbProperties.put("org.jboss.ejb.client.scoped.context", "true"); // Not needed when EJBClientContext.setSelector is called programatically. ATTENTION: Client-Interceptor registration below does not work with this property! BUG?

        final EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(ejbProperties);
        final ConfigBasedEJBClientContextSelector selector = new ConfigBasedEJBClientContextSelector(ejbClientConfiguration);
        EJBClientContext.setSelector(selector);
        EJBClientContext.getCurrent().registerInterceptor(0, new ClientInterceptor());

        final Context ejbContext = new InitialContext(ejbProperties);
        final HelloWorld ejbHelloWorld = (HelloWorld) ejbContext.lookup("ejb:ejbremote-ear/ejbremote-ejb/HelloWorldBean!"+ HelloWorld.class.getName());
        System.out.println(ejbHelloWorld.sayHello());
	}
}
