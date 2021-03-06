package to.etc.domui.injector;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.AbstractPage;
import to.etc.util.PropertyInfo;
import to.etc.webapp.ProgrammerErrorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Access checker which uses the type of the value to check access to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
public class TypedPropertyAccessChecker implements IInjectedPropertyAccessChecker {
	private Map<Class<?>, ITypedValueAccessChecker<?>> m_checkerMap = Collections.emptyMap();

	public synchronized <T> void register(Class<T> clz, ITypedValueAccessChecker<T> check) {
		Map<Class<?>, ITypedValueAccessChecker<?>> map = new HashMap<>(m_checkerMap);
		if(map.put(clz, check) != null)
			throw new ProgrammerErrorException("Duplicate typed access checker for class " + clz.getName());
		m_checkerMap = Collections.unmodifiableMap(map);
	}

	private synchronized Map<Class<?>, ITypedValueAccessChecker<?>> getCheckerMap() {
		return m_checkerMap;
	}

	@Override
	public boolean isAccessAllowed(PropertyInfo info, AbstractPage page, @Nullable Object value) throws Exception {
		return checkAccessSigh(info, page, value);
	}

	private <T> boolean checkAccessSigh(PropertyInfo info, AbstractPage page, @Nullable Object value) throws Exception {
		if(null == value)
			return true;

		ITypedValueAccessChecker<T> checker = findClassChecker(value.getClass());
		if(null == checker)
			return false;
		return checker.isAccessAllowed(info, page, (T) value);
	}

	@Nullable
	private <T> ITypedValueAccessChecker<T> findClassChecker(Class<?> clz) {
		Map<Class<?>, ITypedValueAccessChecker<?>> map = getCheckerMap();
		Class<?> curr = clz;
		for(;;) {
			if(curr == Object.class || curr == null)
				return null;
			ITypedValueAccessChecker<?> checker = map.get(curr);
			if(null != checker) {
				return (ITypedValueAccessChecker<T>) checker;
			}

			curr = curr.getSuperclass();
		}
	}
}
