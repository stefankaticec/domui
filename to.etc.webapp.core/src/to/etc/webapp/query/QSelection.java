package to.etc.webapp.query;

import java.util.*;

import to.etc.webapp.*;

/**
 * Represents a <i>selection</i> of data elements from a database. This differs from
 * a QCriteria in that it collects not one persistent class instance per row but multiple
 * items per row, and each item can either be a persistent class or some property or
 * calculated value (max, min, count et al).
 * 
 * <p>Even though this type has a generic type parameter representing the base object
 * being queried, the list() method for this object will return a List<Object[]> always.</p>
 *
 * <p>QSelection queries return an array of items for each row, and each element
 * of the array is typed depending on it's source. In addition, QSelection queries
 * expose the ability to handle grouping. QSelection criteria behave as and should
 * be seen as SQL queries in an OO wrapping.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QSelection<T> extends QRestrictionsBase {
	final private Class<T>	m_root;
	private final List<QSelectionColumn> m_itemList = Collections.EMPTY_LIST;

	private QSelection(Class<T> clz) {
		m_root = clz;
	}

	/**
	 * Create a selection query based on the specified persistent class.
	 * @param <T>
	 * @param root
	 * @return
	 */
	static public <T> QSelection<T>	create(Class<T> root) {
		return new QSelection<T>(root);
	}

	/**
	 * Returns the persistent class being queried.
	 * @return
	 */
	public Class<T> getBaseClass() {
		return m_root;
	}
	/**
	 * Returns all selected columns.
	 * @return
	 */
	public List<QSelectionColumn> getColumnList() {
		return m_itemList;
	}

	public void	visit(QNodeVisitor v) throws Exception {
		v.visitSelection(this);
	}
	/*--------------------------------------------------------------*/
	/*	CODING:	Object selectors.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Add a column selector to the selection list.
	 */
	protected void	addColumn(QSelectionItem item, String alias) {
		QSelectionColumn	col	= new QSelectionColumn(item, alias);
		m_itemList.add(col);
	}

	/**
	 * Add a simple property selector to the list.
	 * @param f
	 * @param prop
	 * @param alias
	 */
	protected void	addPropertySelection(QSelectionFunction f, String prop, String alias) {
		if(prop == null || prop.length() == 0)
			throw new ProgrammerErrorException("The property for a "+f+" selection cannot be null or empty");
		QPropertySelection	ps	= new QPropertySelection(f, prop);
		addColumn(ps, alias);
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	public QSelection<T>	selectProperty(String property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	public QSelection<T>	selectProperty(String property, String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	public QSelection<T>	max(String property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	public QSelection<T>	max(String property, String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	public QSelection<T>	min(String property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	public QSelection<T>	min(String property, String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return this;
	}
	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	public QSelection<T>	avg(String property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	public QSelection<T>	avg(String property, String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	public QSelection<T>	sum(String property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	public QSelection<T>	sum(String property, String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	public QSelection<T>	count(String property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	public QSelection<T>	count(String property, String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	public QSelection<T>	countDistinct(String property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	public QSelection<T>	countDistinct(String property, String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return this;
	}






	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides to force return type needed for chaining	*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public QSelection<T> add(final QOperatorNode r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#add(to.etc.webapp.query.QOrder)
	 */
	@Override
	public QSelection<T> add(final QOrder r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ascending(java.lang.String)
	 */
	@Override
	public QSelection<T> ascending(final String property) {
		return (QSelection<T>) super.ascending(property);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public QSelection<T> between(final String property, final Object a, final Object b) {
		return (QSelection<T>) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#descending(java.lang.String)
	 */
	@Override
	public QSelection<T> descending(final String property) {
		return (QSelection<T>) super.descending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, double)
	 */
	@Override
	public QSelection<T> eq(final String property, final double value) {
		return (QSelection<T>) super.eq(property, value);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, long)
	 */
	@Override
	public QSelection<T> eq(final String property, final long value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> eq(final String property, final Object value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, double)
	 */
	@Override
	public QSelection<T> ge(final String property, final double value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, long)
	 */
	@Override
	public QSelection<T> ge(final String property, final long value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ge(final String property, final Object value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, double)
	 */
	@Override
	public QSelection<T> gt(final String property, final double value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, long)
	 */
	@Override
	public QSelection<T> gt(final String property, final long value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> gt(final String property, final Object value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ilike(final String property, final Object value) {
		return (QSelection<T>) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#isnotnull(java.lang.String)
	 */
	@Override
	public QSelection<T> isnotnull(final String property) {
		return (QSelection<T>) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#isnull(java.lang.String)
	 */
	@Override
	public QSelection<T> isnull(final String property) {
		return (QSelection<T>) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, double)
	 */
	@Override
	public QSelection<T> le(final String property, final double value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, long)
	 */
	@Override
	public QSelection<T> le(final String property, final long value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> le(final String property, final Object value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> like(final String property, final Object value) {
		return (QSelection<T>) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, double)
	 */
	@Override
	public QSelection<T> lt(final String property, final double value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, long)
	 */
	@Override
	public QSelection<T> lt(final String property, final long value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> lt(final String property, final Object value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, double)
	 */
	@Override
	public QSelection<T> ne(final String property, final double value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, long)
	 */
	@Override
	public QSelection<T> ne(final String property, final long value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ne(final String property, final Object value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	public QSelection<T> or(final QOperatorNode... a) {
		return (QSelection<T>) super.or(a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#sqlCondition(java.lang.String)
	 */
	@Override
	public QSelection<T> sqlCondition(final String sql) {
		return (QSelection<T>) super.sqlCondition(sql);
	}
}
