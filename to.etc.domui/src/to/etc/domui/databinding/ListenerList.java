package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * Threadsafe fast listener list implementation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class ListenerList<V, E extends IChangeEvent<V, E, T>, T extends IChangeListener<V, E, T>> {
	@Nonnull
	static private final Object[] NONE = new Object[0];

	@Nonnull
	private T[] m_listeners = (T[]) NONE;

	/**
	 * Add a new listener to the set.
	 * @param listener
	 */
	public synchronized void addChangeListener(@Nonnull T listener) {
		//-- Already exists?
		final int length = m_listeners.length;
		for(int i = length; --i >= 0;) {
			if(m_listeners[i] == listener)
				return;
		}

		//-- We need a change. Reallocate, then add
		T[] ar = (T[]) new Object[length + 1];
		System.arraycopy(m_listeners, 0, ar, 0, length);
		ar[length] = listener;
		m_listeners = ar;
	}

	/**
	 * Remove the listener if it exists. This leaves a null hole in the array.
	 * @param listener
	 */
	public synchronized void removeChangeListener(@Nonnull T listener) {
		//-- Already exists?
		final int length = m_listeners.length;
		for(int i = length; --i >= 0;) {
			if(m_listeners[i] == listener) {
				m_listeners[i] = null;
				return;
			}
		}
	}

	@Nonnull
	private synchronized T[] getListeners() {
		return m_listeners;
	}

	/**
	 * Remove all listeners.
	 */
	public void clear() {
		m_listeners = (T[]) NONE;
	}

	/**
	 * Call all listeners.
	 * @param event
	 */
	public void fireEvent(@Nonnull E event) throws Exception {
		for(T listener : getListeners()) {
			listener.handleChange(event);
		}
	}
}
