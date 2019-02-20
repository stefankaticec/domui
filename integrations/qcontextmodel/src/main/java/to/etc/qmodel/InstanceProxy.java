package to.etc.qmodel;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This receives all calls to the original object. All calls are delegated to that original
 * object but the data is pickled
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-2-19.
 */
public class InstanceProxy implements InvocationHandler {
	private final ModelContextWrapper m_context;

	private final Object m_unwrapped;

	private final Map<String, Object> m_wrappedValueMap = new HashMap<>(79);

	public <T> InstanceProxy(ModelContextWrapper context, T unwrapped) throws Exception {
		m_context = context;
		m_unwrapped = unwrapped;
	}

	@Override public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
		System.out.println(">> Intercepted " + method);
		if(method.getParameterCount() == 0) {
			//-- Getter
			return handleGetter(o, method);
		} else
			throw new IllegalStateException("Unexpected method: " + method);
	}

	private Object handleGetter(Object o, Method method) throws Exception {
		String propName = method.getName();
		if(propName.startsWith("get"))
			propName = propName.substring(3);
		else if(propName.startsWith("is")) {
			propName = propName.substring(2);
		} else
			throw new IllegalStateException(method + " name has no get or is");

		//-- Did we already do this?
		if(m_wrappedValueMap.containsKey(propName)) {
			return m_wrappedValueMap.get(propName);
		}

		Object value = method.invoke(m_unwrapped);
		if(null == value)
			return null;

		if(List.class.isAssignableFrom(value.getClass())) {
			value = m_context.getWrappableList((List<?>) value);
		} else {
			Class<?> returnType = method.getReturnType();
			ClassMetaModel cmm = MetaManager.findClassMeta(returnType);
			if(cmm.isPersistentClass()) {
				value = m_context.getWrappedInstance(value);
			}
		}
		m_wrappedValueMap.put(propName, value);
		return value;
	}
}

