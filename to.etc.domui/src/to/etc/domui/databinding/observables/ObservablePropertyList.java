package to.etc.domui.databinding.observables;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list.*;

public class ObservablePropertyList<C, T> extends ListenerList<List<T>, ListValueChangeEvent<T>, IListValueChangeListener<T>> implements IObservableListValue<T> {
	@Nonnull
	final private C m_instance;

	@Nonnull
	final private PropertyMetaModel<List<T>> m_property;

	public ObservablePropertyList(@Nonnull C instance, @Nonnull PropertyMetaModel<List<T>> property) {
		m_instance = instance;
		m_property = property;
	}

	//	@Override
	//	@Nonnull
	//	public Class<List<T>> getValueType() {
	//		return m_property.getActualType();
	//	}
	//
	@Override
	@Nullable
	public List<T> getValue() throws Exception {
		return m_property.getValue(m_instance);
	}

	@Override
	public void setValue(@Nullable List<T> value) throws Exception {
		/*
		 * 20130425 jal
		 * The old implementation did a getvalue before the setvalue, and fired an event when the
		 * old and new values changed. This should apparently not be done: the property we're observing
		 * itself will fire an event when modified.
		 */
		m_property.setValue(m_instance, value);
	}

	//	void notifyIfChanged(@Nullable T old, @Nullable T value) {
	//		if(MetaManager.areObjectsEqual(old, value))
	//			return;
	//		ValueDiff<T> vd = new ValueDiff<T>(old, value);
	//		fireEvent(new ValueChangeEvent<T>(this, vd));
	//	}
}
