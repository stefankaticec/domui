package to.etc.domui.hibernate.generic;

import java.util.*;

import org.hibernate.*;
import org.hibernate.engine.*;
import org.hibernate.impl.*;

import to.etc.domui.state.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * This is an HibernateContext which uses automatic reattachment of all nodes to prevent
 * all kinds of crappy Hibernate-related shit.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 22, 2008
 */
public class HibernateReattachingDataContext extends HibernaatjeBaseContext {
	private final List<Object> m_hibernatePersistedObjects = new ArrayList<Object>();

	public HibernateReattachingDataContext(final HibernateSessionMaker sessionMaker, QDataContextSource src) {
		super(sessionMaker, src);
	}

	/**
	 * Overridden to allow for automagic re-attachment of crap.
	 * @see to.etc.domui.hibernate.generic.HibernaatjeBaseContext#getSession()
	 */
	@Override
	public Session getSession() throws Exception {
		if(m_session == null) {
			m_session = m_sessionMaker.makeSession();

			//-- jal 20080822 reattach all previously-loaded entities.
			long ts = System.nanoTime();
			for(Object el : m_hibernatePersistedObjects) {
				m_session.update(el); // What a beaut: this makes the object DIRTY, causing a full write @ end. What a piece of crap.
			}
			ts = System.nanoTime() - ts;
			System.out.println("hib: re-attached " + m_hibernatePersistedObjects.size() + " objects in " + StringTool.strNanoTime(ts));
			m_hibernatePersistedObjects.clear();
		}
		return m_session;
	}

	@Override
	public void conversationDestroyed(final ConversationContext cc) throws Exception {
		conversationDetached(cc);
	}

	@Override
	public void conversationDetached(final ConversationContext cc) throws Exception {
		if(m_session == null)
			return;

		/*
		 * jal 20080822 Attempt to fix org.hibernate.NonUniqueObjectException by saving all objects currently in the session and
		 * reattaching them automagically at conversation attach time.
		 */
		long ts = System.nanoTime();
		SessionImpl sim = (SessionImpl) m_session;
		StatefulPersistenceContext spc = (StatefulPersistenceContext) sim.getPersistenceContext();
		Map< ? , ? > flups = spc.getEntitiesByKey();
		m_hibernatePersistedObjects.clear();
		for(Object ent : flups.values()) {
			//			System.out.println(">> Got entity: "+ent);
			m_hibernatePersistedObjects.add(ent);
		}
		ts = System.nanoTime() - ts;
		System.out.println("hib: saved " + flups.size() + " persisted objects in the conversation for reattachment in " + StringTool.strNanoTime(ts));

		close();
	}
}
