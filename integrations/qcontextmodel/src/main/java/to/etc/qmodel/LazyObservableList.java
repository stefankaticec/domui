package to.etc.qmodel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.databinding.ListenerList;
import to.etc.domui.databinding.list.ListChange;
import to.etc.domui.databinding.list.ListChangeAdd;
import to.etc.domui.databinding.list.ListChangeDelete;
import to.etc.domui.databinding.list.ListChangeModify;
import to.etc.domui.databinding.list2.IListChangeListener;
import to.etc.domui.databinding.list2.ListChangeEvent;
import to.etc.domui.databinding.observables.IObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An ObservableList which lazily copies its source. Used to wrap any lazy-loaded
 * list property in such a way that just wrapping the property value will not cause
 * the list to be loaded.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 19, 2019
 */
public class LazyObservableList<T> extends ListenerList<T, ListChangeEvent<T>, IListChangeListener<T>> implements IObservableList<T> {
	private final List<T> m_originalList;

	@Nullable
	private List<T> m_list;

	/** When set this becomes an ordered model. */
	@Nullable
	private Comparator<T> m_comparator;

	@Nullable
	private List<ListChange<T>> m_currentChange;

	private int m_changeNestingCount;

	public LazyObservableList(@NonNull List<T> list) {
		m_originalList = list;
	}

	private List<ListChange<T>> startChange() {
		List<ListChange<T>> currentChange = m_currentChange;
		if(null == currentChange) {
			m_changeNestingCount = 1;
			m_currentChange = currentChange = new ArrayList<>();
		} else
			m_changeNestingCount++;
		return currentChange;
	}

	private void finishChange() {
		if(m_changeNestingCount <= 0)
			throw new IllegalStateException("Unbalanced startChange/finishChange");
		m_changeNestingCount--;

		if(m_changeNestingCount == 0) {
			List<ListChange<T>> currentChange = m_currentChange;
			m_currentChange = null;
			if(null == currentChange)
				throw new IllegalStateException("Logic error: no change list");
			fireEvent(new ListChangeEvent<>(this, currentChange));
		}
	}

	@Override
	public int size() {
		return getList().size();
	}

	@Override
	public boolean isEmpty() {
		return getList().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return getList().contains(o);
	}

	@NonNull
	@Override
	public Iterator<T> iterator() {
		return getList().iterator();
	}

	@Override
	public Object[] toArray() {
		return getList().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return getList().toArray(a);
	}

	@Override
	public boolean add(T e) {
		int index;
		Comparator<T> comparator = m_comparator;
		if(null == comparator) {
			index = size();
			getList().add(e);
		} else {
			index = Collections.binarySearch(getList(), e, comparator);
			if(index < 0)
				index = -(index + 1);
			getList().add(index, e);
		}
		List<ListChange<T>> el = startChange();
		el.add(new ListChangeAdd<>(index, e));
		finishChange();
		return true;
	}

	@Override
	public boolean remove(Object o) {
		int indx = getList().indexOf(o);
		if(indx < 0)
			return false;
		getList().remove(indx);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeDelete<T>(indx, (T) o));
		finishChange();
		return true;
	}

	@Override
	public boolean containsAll(Collection< ? > c) {
		return getList().containsAll(c);
	}

	@Override
	public boolean addAll(Collection< ? extends T> c) {
		List<ListChange<T>> el = startChange();
		Comparator<T> comparator = m_comparator;
		if(null == comparator) {
			getList().addAll(c);
			int indx = size();
			for(T v : c) {
				el.add(new ListChangeAdd<T>(indx++, v));
			}
		} else {
			for(T v : c) {
				add(v);
			}
		}
		finishChange();
		return c.size() > 0;
	}

	@Override
	public boolean addAll(int index, Collection< ? extends T> c) {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot add by index on a sorted model: the sorting order determines the insert index");
		boolean res = getList().addAll(index, c);

		List<ListChange<T>> el = startChange();
		int indx = index;
		for(T v : c) {
			el.add(new ListChangeAdd<>(indx++, v));
		}
		finishChange();
		return res;
	}

	@Override
	public boolean removeAll(Collection< ? > c) {
		List<ListChange<T>> el = startChange();
		boolean done = false;
		for(Object v : c) {
			int indx = indexOf(v);
			if(indx >= 0) {
				getList().remove(indx);
				el.add(new ListChangeDelete<T>(indx, (T) v));
				done = true;
			}
		}
		finishChange();
		return done;
	}

	@Override
	public boolean retainAll(Collection< ? > c) {
		List<ListChange<T>> el = startChange();
		boolean done = false;
		for(int i = size(); --i >= 0;) {
			T cv = getList().get(i);
			if(!c.contains(cv)) {
				getList().remove(i);
				el.add(new ListChangeDelete<T>(i, cv));
				done = true;
			}
		}
		finishChange();
		return done;
	}

	@Override
	public void clear() {
		List<ListChange<T>> el = startChange();
		for(int i = size(); --i >= 0;) {
			T cv = getList().get(i);
			el.add(new ListChangeDelete<T>(i, cv));
		}
		finishChange();
		getList().clear();
	}

	@Override
	public boolean equals(Object o) {
		return getList().equals(o);
	}

	@Override
	public int hashCode() {
		return getList().hashCode();
	}

	@NonNull
	@Override
	public T get(int index) {
		return getList().get(index);
	}

	@Override
	public T set(int index, T element) {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot set by index on a sorted model: the sorting order determines the insert index");

		T res = getList().set(index, element);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeModify<T>(index, res, element));
		finishChange();
		return res;
	}

	@Override
	public void add(int index, T element) {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot add by index on a sorted model: the sorting order determines the insert index");
		getList().add(index, element);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeAdd<T>(index, element));
		finishChange();
	}

	@Override
	public T remove(int index) {
		T res = getList().remove(index);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeDelete<>(index, res));
		finishChange();
		return res;
	}

	@Override
	public int indexOf(Object o) {
		return getList().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return getList().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return getList().listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return getList().listIterator(index);
	}

	@NonNull
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return getList().subList(fromIndex, toIndex);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Sortable handling.									*/
	/*--------------------------------------------------------------*/
	/**
	 * When set the list will be kept ordered.
	 */
	@Nullable
	public Comparator<T> getComparator() {
		return m_comparator;
	}

	/**
	 * Sets a new comparator to use. This resorts the model, if needed, causing a full model update.
	 */
	public void setComparator(@Nullable Comparator<T> comparator) throws Exception {
		if(m_comparator == comparator)
			return;
		m_comparator = comparator;
		if(comparator != null) {
			resort();
		}
	}

	private void resort() throws Exception {
		startChange();
		Collections.sort(getList(), m_comparator);
		finishChange();
	}

	private List<T> getList() {
		List<T> list = m_list;
		if(null == list) {
			list = m_list = new ArrayList<>(m_originalList);
		}
		return list;
	}

	private void setList(List<T> list) {
		m_list = list;
	}
}
