package org.jboss.qa.ochaloup;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

public final class EjbCallUtils {
	private EjbCallUtils() {
		// can't be intialized
	}

	public static final String HOST = System.getProperty("call.host", "localhost");
	public static final String PORT = System.getProperty("call.port");
	// echo 'user=c5568adea472163dfc00c19c6348a665' >> standalone/configuration/application-users.properties
	public static final String SECURITY_USERNAME = System.getProperty("call.username", "user");
	public static final String SECURITY_PASSWORD = System.getProperty("call.password", "user");

	public static final <T> T lookupEjbOutboundBinding(final String earName, final String jarName, final Class<?> beanClass, final Class<T> remoteClass) {
        String beanLookup = getEjbLookupString(earName, jarName, beanClass, remoteClass);
		String lookupInfo = String.format("bean by lookup '%s' (expecting outbound connection defined)",
				beanLookup, HOST, PORT, SECURITY_USERNAME, SECURITY_PASSWORD);

		System.out.println("Looking at: " + lookupInfo);
		final Properties props = new Properties();
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		return doLookup(beanLookup, remoteClass, props);
	}

	/**
	 * <p>
	 * Remote naming
	 * <p>
	 * Strangely seems not working... ?
	 */
	public static final <T> T lookupRemoteNaming(final String earName, final String jarName, final Class<?> beanClass, final Class<T> remoteClass) {
		Properties props = new Properties();
		props.put(Context.PROVIDER_URL, "http-remoting://" + HOST + ":" + PORT);
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		props.put(Context.SECURITY_PRINCIPAL, SECURITY_USERNAME);
		props.put(Context.SECURITY_CREDENTIALS, SECURITY_PASSWORD);
		// to avoid: java.lang.IllegalStateException: EJBCLIENT000025: No EJB receiver available
		props.put("jboss.naming.client.ejb.context", true);

		String beanLookup = getRemoteNamingLookupString(earName, jarName, beanClass, remoteClass);
		String lookupInfo = String.format("bean by lookup '%s' to '%s:%s' with credentials '%s/%s'",
				beanLookup, HOST, PORT, SECURITY_USERNAME, SECURITY_PASSWORD);

		System.out.println("Looking at: " + lookupInfo);
		return doLookup(beanLookup, remoteClass, props);
	}

	/**
	 * <p>
	 * EJB client<br>
	 * <p>
	 * From:<br>
	 * http://git.app.eng.bos.redhat.com/git/jbossqe/eap-tests-ejb.git/tree/ejb-client-qa-tests/src/test/java/org/jboss/qa/ejbclient/jndi/InitialContextDirectoryScoped.java
	 */
	public static final <T> T lookupEjbClient(final String earName, final String jarName, final Class<?> beanClass, final Class<T> remoteClass) {
        System.setProperty("org.jboss.ejb.client.view.annotation.scan.enabled", "true");  // without this, CompressionHint for requests won't work
        Properties env = new Properties();
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        env.put("org.jboss.ejb.client.scoped.context", "true");
        env.put("endpoint.name", "client-endpoint");
        env.put("remote.connections", "main");
        env.put("remote.connection.main.protocol", "http-remoting");
        env.put("remote.connection.main.host", HOST);
        env.put("remote.connection.main.port", PORT);
        env.put("remote.connection.main.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
        env.put("remote.connection.main.connect.options.org.xnio.Options.SSL_STARTTLS", "true");
        env.put("remote.connection.main.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "true");
        env.put("remote.connection.main.connect.options.org.xnio.Options.SSL_ENABLED", "false");
        env.put("remote.connection.main.username", SECURITY_USERNAME);
        env.put("remote.connection.main.password", SECURITY_PASSWORD);
        env.put("remote.connection.main.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER");

        String beanLookup = getEjbLookupString(earName, jarName, beanClass, remoteClass);
		String lookupInfo = String.format("bean by lookup '%s' to '%s:%s' with credentials '%s/%s'",
				beanLookup, HOST, PORT, SECURITY_USERNAME, SECURITY_PASSWORD);

		System.out.println("Looking at: " + lookupInfo);
		return doLookup(beanLookup, remoteClass, env);
	}

	/**
	 * <p>
	 * Using the proprietary JBoss EJB Client API.<br>
	 * See e.g. https://gist.github.com/jbandi/6287518
	 * <p>
	 * Probaly can't be used when running on server:<br>
	 * <code>java.lang.SecurityException: EJBCLIENT000021: EJB client context selector may not be changed</code>
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
        // EJBClientContext.getCurrent().registerInterceptor(0, new ClientInterceptor()); - probably if needed a home baked interceptor

        String beanLookup = getEjbLookupString(earName, jarName, beanClass, remoteClass);
		String lookupInfo = String.format("bean by lookup '%s' to '%s:%s' with credentials '%s/%s'",
				beanLookup, HOST, PORT, SECURITY_USERNAME, SECURITY_PASSWORD);

		System.out.println("Looking at: " + lookupInfo);
		return doLookup(beanLookup, remoteClass, ejbProperties);
	}

	private static String getEjbLookupString(final String earName, final String jarName, final Class<?> beanClass, final Class<?> remoteClass) {
		return "ejb:" + earName + "/" + jarName + "//" + beanClass.getSimpleName() + "!" + remoteClass.getName();
	}
	
	private static String getRemoteNamingLookupString(final String earName, final String jarName, final Class<?> beanClass, final Class<?> remoteClass) {
		// (isStateful ? "?stateful" : "");
		return earName + "/" + jarName + "/" + beanClass.getSimpleName() + "!" + remoteClass.getName();
	}

	private static <T> T doLookup(final String beanLookupString, final Class<T> remoteClass, final Properties props) {
		Context context = null;
		try {
			context = new InitialContext(props);
			Thread.sleep(1 * 1000);
			return remoteClass.cast(context.lookup(beanLookupString));
		} catch (Exception e) {
			throw new RuntimeException("Can't get: " + beanLookupString, e);
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
