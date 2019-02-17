package to.etc.qmodel;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-2-19.
 */
public class InstanceProxy implements InvocationHandler {
	private final Object m_unwrapped;

	public <T> InstanceProxy(T unwrapped) {
		m_unwrapped = unwrapped;
	}

	@BindingPriority(99)
	public Object methodInterceptor(@SuperMethod Method sup, @AllArguments Object... args) {
		System.out.println(">> Intercepted " + sup);
		return null;
	}

	@Override public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
		System.out.println(">> Intercepted " + method);

		return method.invoke(m_unwrapped, objects);
	}
}

