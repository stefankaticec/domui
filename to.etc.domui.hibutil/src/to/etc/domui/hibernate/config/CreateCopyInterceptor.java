package to.etc.domui.hibernate.config;

import javax.annotation.*;

import org.hibernate.*;
import org.hibernate.event.*;
import org.hibernate.proxy.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * This session interceptor delegates load events to the before-image load cache.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 13, 2014
 */
public class CreateCopyInterceptor extends EmptyInterceptor {
	@Nonnull
	final private IBeforeImageCache m_cache;

	public CreateCopyInterceptor(@Nonnull IBeforeImageCache cache) {
		m_cache = cache;
	}

	/**
	 * Whenever an entity is loaded make sure that a "before" image of it exists.
	 *
	 * EXPERIMENTAL This uses a non-hibernate method which is added to Hibernate's source code.
	 *
	 * @see org.hibernate.EmptyInterceptor#onAfterLoad(org.hibernate.event.PostLoadEvent)
	 */
	@Override
	public void onAfterLoad(@Nonnull PostLoadEvent loadevent) {
		Object instance = loadevent.getEntity();
		if(null == instance)
			throw new IllegalStateException("entity instance null in interceptor!?");

		System.out.println("Interceptor: afterload " + MetaManager.identify(instance));
		try {
			Class real = Hibernate.getClass(instance);

			Object copy = m_cache.createImage(real, instance, true);
			copyProperties(copy, instance);				// Copy whatever properties we can
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * Copy all properties from src to dst, taking the different types into consideration. All "simple"
	 * types can just be copied by reference, but we need to take special care of the following:
	 * <ul>
	 *	<li>ManyToOne properties can refer either to an existing instance in the cache OR they can be "unloaded". If they are loaded
	 *		then we must replace the reference to the "before" image for that thing. If the thing is currently unloaded we must make
	 *		sure we create the before image already, but with a "unloaded" indication.</li>
	 * </ul>
	 * @param copy
	 * @param instance
	 */
	private <T> void copyProperties(@Nonnull T dst, @Nonnull T src) throws Exception {
		for(PropertyMetaModel< ? > pmm : MetaManager.findClassMeta(src.getClass()).getProperties()) {
			System.out.println("   >> copy property " + pmm + " of " + src.getClass());
			copyProperty(dst, src, pmm);
		}
	}

	private <T, V> void copyProperty(@Nonnull T dst, @Nonnull T src, @Nonnull PropertyMetaModel<V> pmm) throws Exception {
		if(pmm.getReadOnly() == YesNoType.YES)					// Cannot set readonlies
			return;
		if(pmm.isTransient())									// We don't wanna play with transients.
			return;

		V value = pmm.getValue(src);							// Get the source instance.

		switch(pmm.getRelationType()){
			case NONE:
				pmm.setValue(dst, value);						// Just copy
				break;

			case DOWN:
				System.out.println("TODO: 'down' relation");
				break;

			case UP:
				if(pmm.getName().equals("btwCode"))
					System.out.println("Gotcha");
				if(value != null)
					value = convertParentRelation(value);
				pmm.setValue(dst, value);
				break;

		}
	}

	/**
	 * If the instance is a "loaded" one then just get it's before image: it must be found.
	 *
	 * @param src
	 * @return
	 */
	private <V> V convertParentRelation(@Nonnull V src) throws Exception {
		if(Hibernate.isInitialized(src)) {						// Loaded?
			//-- Replace the instance with the before image of that instance.
			V before = m_cache.findBeforeImage(src);
			if(null == before)
				throw new IllegalStateException("The 'before' image for " + MetaManager.identify(src) + " cannot be found, even though it is loaded by Hibernate!?");
			return before;
		}

		/*
		 * The instance is not loaded 8-/. We need to create a copy that is marked as "uninitialized".
		 */
		Class<V> realclass = getProxyClass(src);
		V copy = m_cache.createImage(realclass, src, false);
		if(m_cache.wasNew()) {
			//-- We need to dup the PK and insert it into the copy. The PK itself can be compound and so also hold data to be copied 8-/
			ClassMetaModel cmm = MetaManager.findClassMeta(src.getClass());
			PropertyMetaModel< ? > pkmm = cmm.getPrimaryKey();
			if(null == pkmm)
				throw new IllegalStateException("Cannot locate the private key property for class " + cmm);
			copyProperty(copy, src, pkmm);
		}
		return copy;
	}

	@Nonnull
	static private <T> Class<T> getProxyClass(@Nonnull T proxy) {
		if(proxy instanceof HibernateProxy) {
			return ((HibernateProxy) proxy).getHibernateLazyInitializer().getPersistentClass();
		} else {
			return (Class<T>) proxy.getClass();
		}
	}

}
