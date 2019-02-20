package to.etc.qmodel;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-2-19.
 */
public class ModelContextWrapper extends QDataContextWrapper implements QDataContext {
	private final Map<Class<?>, Map<Object, Object>> m_entityMap = new HashMap<>();

	private final ProxyGenerator m_proxyGenerator = new ProxyGenerator();

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

	<T> T wrapInstance(T unwrapped) throws Exception {
		Class<?> clz = unwrapped.getClass();
		return m_proxyGenerator.createPojoProxy(this, unwrapped);
	}
	//private Object getPK(Class<?> clz, Object )

	static public <T> Class<T> unwrapName(Class<? extends T> rootClz) {
		return (Class<T>) rootClz;
	}

	<T, P> P getPrimaryKey(T instance) throws Exception {
		if(instance instanceof IIdentifyable<?>) {
			return (P) ((IIdentifyable<P>) instance).getId();
		}

		PropertyMetaModel<?> pkPmm = MetaManager.findClassMeta(instance.getClass()).getPrimaryKey();
		if(null == pkPmm)
			throw new IllegalStateException("Unknown primary key property in class " + instance.getClass().getName());
		return (P)pkPmm.getValue(instance);
	}

	public <T> T getWrappedInstance(@NonNull T value) throws Exception {
		Object primaryKey = getPrimaryKey(value);
		if(null == primaryKey)
			throw new IllegalStateException("The primary key for " + value + " is null - that should not happen");
		Map<Object, Object> k2eMap = m_entityMap.computeIfAbsent(unwrapName(value.getClass()), a -> new HashMap<>());
		Object entityObj = k2eMap.get(primaryKey);
		if(null != entityObj)
			return (T) entityObj;

		//-- We need to wrap
		T wrapped = wrapInstance(value);
		k2eMap.put(primaryKey, wrapped);
		return wrapped;
	}

	public <T> List<T> getWrappableList(List<T> value) {
		return new LazyObservableList<>(value);
	}
}
