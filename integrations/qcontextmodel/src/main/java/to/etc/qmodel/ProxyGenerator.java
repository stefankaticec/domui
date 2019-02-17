package to.etc.qmodel;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Generates the wrappers around data classes using ByteBuddy.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-2-19.
 */
final public class ProxyGenerator {
	public <T> T createPojoProxy(T original) throws Exception {
		Class<T> origClass = (Class<T>) original.getClass();
		Class<T> proxyClass = getProxyClass(origClass);
		InstanceProxy ip = new InstanceProxy(original);
		Field handler = proxyClass.getField("__handler");

		T proxy = proxyClass.newInstance();
		handler.setAccessible(true);
		handler.set(proxy, ip);
		return proxy;
	}

	public <T> Class<T> getProxyClass(Class<T> originalClass) throws Exception {
		return createProxyClass(originalClass);
	}

	public <T> Class<T> createProxyClass(Class<T> originalClass) throws Exception {
		Unloaded<T> unloaded = new ByteBuddy()
			.subclass(originalClass)
			.defineField("__handler", InstanceProxy.class, Modifier.PUBLIC)
			.method(ElementMatchers.nameStartsWith("get").and(ElementMatchers.takesArguments(0)))
			//.intercept(MethodDelegation.toField("__handler"))
			.intercept(InvocationHandlerAdapter.toField("__handler"))
			.make();
		unloaded.saveIn(new File("/tmp/bybu.class"));

		Class<? extends T> proxyClass = unloaded
			.load(getClass().getClassLoader())
			.getLoaded();

		return (Class<T>) proxyClass;
	}

}
