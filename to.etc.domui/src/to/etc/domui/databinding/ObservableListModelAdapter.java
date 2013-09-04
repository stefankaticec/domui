package to.etc.domui.databinding;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.tbl.*;

/**
 * EXPERIMENTAL This adapter creates a {@link ITableModel} from an {@link IObservableList} instance, converting the ObservableList
 * events to table events. This can be used to keep any {@link DataTable} in sync with an observable list.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public class ObservableListModelAdapter<T> implements ITableModel<T> {
	@Nonnull
	final private IObservableList<T> m_list;

	private Map<ITableModelListener<T>, EvListener> m_lmap = new HashMap<ITableModelListener<T>, EvListener>();

	public ObservableListModelAdapter(@Nonnull IObservableList<T> list) {
		m_list = list;
	}

	@Override
	public List<T> getItems(int start, int end) throws Exception {
		return m_list.subList(start, end);
	}

	@Override
	public int getRows() throws Exception {
		return m_list.size();
	}

	@Override
	public void addChangeListener(ITableModelListener<T> l) {
		if(m_lmap.containsKey(l))
			return;
		EvListener el = new EvListener(l);
		m_list.addChangeListener(el);
		m_lmap.put(l, el);
	}

	@Override
	public void removeChangeListener(ITableModelListener<T> l) {
		EvListener el = m_lmap.remove(l);
		if(null == el)
			return;
		m_list.removeChangeListener(el);
	}

	@Override
	public void refresh() {}

	private class EvListener implements IListChangeListener<T> {
		final private ITableModelListener<T> m_modelListener;

		public EvListener(ITableModelListener<T> l) {
			m_modelListener = l;
		}

		/**
		 * Propagate {@link ListChangeEvent} events to a {@link ITableModelListener}.
		 * @see to.etc.domui.databinding.IChangeListener#handleChange(to.etc.domui.databinding.IChangeEvent)
		 */
		@Override
		public void handleChange(ListChangeEvent<T> event) throws Exception {
			event.visit(new IListChangeVisitor<T>() {
				@Override
				public void visitAdd(ListChangeAdd<T> l) throws Exception {
					m_modelListener.rowAdded(ObservableListModelAdapter.this, l.getIndex(), l.getValue());
				}

				@Override
				public void visitDelete(ListChangeDelete<T> l) throws Exception {
					m_modelListener.rowDeleted(ObservableListModelAdapter.this, l.getIndex(), l.getValue());
				}

				@Override
				public void visitModify(ListChangeModify<T> l) throws Exception {
					m_modelListener.rowModified(ObservableListModelAdapter.this, l.getIndex(), l.getNewValue());
				}
			});
		}
	}
}
