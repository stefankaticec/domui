package to.etc.qmodel;

import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-2-19.
 */
public class ModelContextWrapper extends QDataContextWrapper implements QDataContext {
	private final Map<Class<?>, Map<Object, Object>> m_entityMap = new HashMap<>();

	public ModelContextWrapper(QDataContext wrapped) {
		super(wrapped);
	}

	@Override public <T> T get(Class<T> clz, Object pk) throws Exception {
		Map<Object, Object> k2eMap = m_entityMap.computeIfAbsent(unwrapName(clz), a -> new HashMap<>());
		Object entityObj = k2eMap.get(pk);
		if(null != entityObj)
			return (T) entityObj;

		//-- We need to query and wrap
		T unwrapped = super.get(clz, pk);
		T wrapped = wrapInstance(unwrapped);
		k2eMap.put(pk, wrapped);
		return wrapped;
	}

	private <T> T wrapInstance(T unwrapped) {
		Class<?> clz = unwrapped.getClass();
		return unwrapped;
	}
	//private Object getPK(Class<?> clz, Object )

	static public <T> Class<T> unwrapName(Class<? extends T> rootClz) {
		return (Class<T>) rootClz;
	}

}
