package to.etc.domui.kotlin.criteria

import to.etc.webapp.query.QCriteria
import kotlin.reflect.KClass

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-2-18.
 */

/**
 * Allow KClass.qCriteria() to create a criterion.
 */
inline fun <reified T: Any> KClass<T>.qCriteria() : QCriteria<T> {
	return QCriteria.create(T::class.java)
}
