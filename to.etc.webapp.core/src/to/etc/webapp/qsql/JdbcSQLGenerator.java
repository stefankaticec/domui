/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.qsql;

import java.util.*;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * Generate a SQL query from a QCriteria selection using the poor man's JDBC code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class JdbcSQLGenerator extends QNodeVisitorBase {
	private PClassRef m_root;

	private JdbcClassMeta m_rootMeta;

	private Map<String, PClassRef> m_tblMap = new HashMap<String, PClassRef>();

	private StringBuilder m_fields = new StringBuilder();

	/** The list of all retrievers for a single row */
	private List<IInstanceMaker> m_retrieverList = new ArrayList<IInstanceMaker>();

	private int m_nextFieldIndex = 1;

	private StringBuilder m_where = new StringBuilder();

	private StringBuilder m_order;

	private int m_nextWhereIndex = 1;

	private int m_curPrec = 0;

	private List<IQValueSetter> m_valList = new ArrayList<IQValueSetter>();

	/** FIXME Need some better way to set this */
	private boolean m_oracle = true;

	private int m_start, m_limit;

	private int m_timeout = -1;

	private String m_sql;

	@Override
	public void visitCriteria(@Nonnull QCriteria< ? > qc) throws Exception {
		m_root = new PClassRef(qc.getBaseClass(), "this_");
		m_tblMap.put(m_root.getAlias(), m_root);
		m_rootMeta = JdbcMetaManager.getMeta(qc.getBaseClass());
		m_start = qc.getStart();
		m_limit = qc.getLimit();
		if(qc.getTimeout() < 0)
			m_timeout = 60;
		else if(qc.getTimeout() != 0)
			m_timeout = qc.getTimeout();

		generateClassGetter(m_root);
		super.visitCriteria(qc);

		/*
		 * Generate SQL. Most of this is disgusting and should be replaced by a SN* tree passed to a database-dependent handler.
		 */
		StringBuilder sb = new StringBuilder(256);
		boolean limiting = qc.getLimit() > 0 || qc.getStart() > 0;
		if(m_oracle && limiting) {
			sb.append("select * from (");
		}

		sb.append("select ");
		sb.append(m_fields);
		sb.append(" from ");

		JdbcClassMeta cm = JdbcMetaManager.getMeta(m_root.getDataClass());
		sb.append(cm.getTableName());
		sb.append(" ");
		sb.append(m_root.getAlias());

		if(m_where.length() > 0) {
			sb.append(" where ");
			sb.append(m_where);
		}
		if(m_order != null) {
			sb.append(" order by ");
			sb.append(m_order);
		}
		if(m_oracle && limiting) {
			sb.append(") where");
			if(qc.getStart() > 0)
				sb.append(" rownum >= ").append(qc.getStart());
			if(qc.getLimit() > 0) {
				if(qc.getStart() > 0)
					sb.append(" and");
				sb.append(" rownum <= ").append(qc.getLimit());
			}
		}
		m_sql = sb.toString();
	}

	@Override
	public void visitSelection(@Nonnull QSelection< ? > qc) throws Exception {
		m_root = new PClassRef(qc.getBaseClass(), "this_");
		m_tblMap.put(m_root.getAlias(), m_root);
		m_rootMeta = JdbcMetaManager.getMeta(qc.getBaseClass());
		m_timeout = 60;


		m_retrieverList.add(new SelectorColumnsResultMaker(qc));
		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());

		StringBuilder sb = new StringBuilder(256);
		sb.append("select ");

		final JdbcClassMeta cm = JdbcMetaManager.getMeta(m_root.getDataClass()); // Will throw exception if not proper jdbc class.

		/**
		 * Jdbc selection query renderer has specific overrides in rendering selection columns.
		 */
		QQueryRenderer renderer = new QQueryRenderer() {

			@Override
			public void visitPropertySelection(QPropertySelection n) throws Exception {
				int currentColumn = getCurrentColumn();
				if(currentColumn > 0) {
					append(",");
				}
				setCurrentColumn(currentColumn + 1);
				//fix needed for COUNT_DISTINCT, correct syntax is 'select (distinct propXXX)'
				if(n.getFunction().equals(QSelectionFunction.COUNT_DISTINCT)) {
					append("count ");
					append("(distinct ");
					append(cm.findProperty(n.getProperty()).getColumnName());
					append(")");
				} else {
					append(n.getFunction().name().toLowerCase());
					append("(");
					append(cm.findProperty(n.getProperty()).getColumnName());
					append(")");
				}
			}
		};
		renderer.visitSelectionColumns(qc);

		sb.append(renderer.toString());

		sb.append(" from ");

		sb.append(cm.getTableName());
		sb.append(" ");
		sb.append(m_root.getAlias());

		if(m_where.length() > 0) {
			sb.append(" where ");
			sb.append(m_where);
		}

		if(m_order != null) {
			sb.append(" order by ");
			sb.append(m_order);
		}
		m_sql = sb.toString();
	}

	private String getColumnRef(PClassRef ref, String name) {
		return ref.getAlias() + "." + name;
	}

	/**
	 * Generate getter code for an entire class instance from a result set.
	 * @param root
	 * @throws Exception
	 */
	private void generateClassGetter(PClassRef root) throws Exception {
		JdbcClassMeta cm = JdbcMetaManager.getMeta(root.getDataClass()); // Will throw exception if not proper jdbc class.
		int startIndex = m_nextFieldIndex;
		for(JdbcPropertyMeta pm : cm.getPropertyList()) {
			if(!pm.isTransient()) {
				generatePropertyGetter(root, pm);
			}
		}
		m_retrieverList.add(new ClassInstanceMaker(root, startIndex, cm));
	}

	private void addSelectColumn(PClassRef root, String name) {
		if(m_fields.length() != 0)
			m_fields.append(",");
		m_fields.append(getColumnRef(root, name));
		m_nextFieldIndex++;
	}

	private void generatePropertyGetter(PClassRef root, JdbcPropertyMeta pm) throws Exception {
		for(String col : pm.getColumnNames())
			addSelectColumn(root, col);
	}

	public String getSQL() throws Exception {
		return m_sql;
	}

	public List<IQValueSetter> getValList() {
		return m_valList;
	}

	public List<IInstanceMaker> getRetrieverList() {
		return m_retrieverList;
	}

	public JdbcQuery< ? > getQuery() throws Exception {
		return new JdbcQuery<Object>(getSQL(), m_retrieverList, m_valList, m_start, m_limit, m_timeout);
	}

	@Override
	public void visitOrder(@Nonnull QOrder o) throws Exception {
		if(m_order == null)
			m_order = new StringBuilder();
		JdbcPropertyMeta pm = resolveProperty(o.getProperty());
		for(String col : pm.getColumnNames()) {
			if(m_order.length() > 0)
				m_order.append(",");
			m_order.append(getColumnRef(m_root, col));
			switch(o.getDirection()){
				default:
					throw new IllegalStateException("Bad order: " + o.getDirection());
				case ASC:
					m_order.append(" asc");
					break;
				case DESC:
					m_order.append(" desc");
					break;
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Restrictions rendering.								*/
	/*--------------------------------------------------------------*/
	private void appendWhere(String s) {
		m_where.append(s);
	}

	/**
	 * Render an operator set.
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitMulti(to.etc.webapp.query.QMultiNode)
	 */
	@Override
	public void visitMulti(@Nonnull QMultiNode n) throws Exception {
		if(n.getChildren().size() == 0)
			return;
		if(n.getChildren().size() == 1) { // Should not really happen
			n.getChildren().get(0).visit(this);
			return;
		}
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");
		int ct = 0;
		for(QOperatorNode c : n.getChildren()) {
			if(ct++ > 0)
				appendOperation(n.getOperation());

			//-- Visit lower
			c.visit(this);
		}
		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitPropertyComparison(@Nonnull QPropertyComparison n) throws Exception {
		//-- Lookup the property name. For now it cannot be dotted
		JdbcPropertyMeta pm = resolveProperty(n.getProperty());
		if(pm.isCompound()) {
			//-- Compound criteria work for EQ and NE only.
			if(n.getOperation() != QOperation.EQ && n.getOperation() != QOperation.NE)
				throw new QQuerySyntaxException("The " + n.getOperation() + " operation is not supported on compound property " + n.getProperty());
			generateCompoundComparison(n, pm);
			return;
		}

		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		if(n.getOperation() == QOperation.ILIKE && m_oracle) {
			appendWhere("upper(");
			appendWhere(getColumnRef(m_root, pm.getColumnName()));
			appendWhere(") like upper(");

			if(n.getExpr() instanceof QLiteral) {
				appendLikeValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new QQuerySyntaxException("Unexpected argument to " + n + ": " + n.getExpr());
			appendWhere(")");
		} else if(n.getOperation() == QOperation.ILIKE || n.getOperation() == QOperation.LIKE) {
			appendWhere(getColumnRef(m_root, pm.getColumnName()));
			appendOperation(n.getOperation());

			if(n.getExpr() instanceof QLiteral) {
				appendLikeValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new QQuerySyntaxException("Unexpected argument to " + n + ": " + n.getExpr());
		} else {
			appendWhere(getColumnRef(m_root, pm.getColumnName()));
			appendOperation(n.getOperation());

			if(n.getExpr() instanceof QLiteral) {
				appendValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new QQuerySyntaxException("Unexpected argument to " + n + ": " + n.getExpr());
		}
		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	/**
	 * Generate a compound eq/ne comparison.
	 * @param n
	 * @param pm
	 */
	private void generateCompoundComparison(QPropertyComparison n, JdbcPropertyMeta pm) {
		//-- Make sure the value instance passed is of the compound's type.
		if(!(n.getExpr() instanceof QLiteral))
			throw new QQuerySyntaxException("Unexpected argument to " + n + ": " + n.getExpr());
		QLiteral ql = (QLiteral) n.getExpr();
		Object inst = ql.getValue();
		if(inst != null) {
			if(!pm.getActualClass().isAssignableFrom(inst.getClass()))
				throw new QQuerySyntaxException("The value of type " + inst.getClass() + " is not assignment-compatible with the compound type=" + pm.getActualClass() + " in property "
					+ n.getProperty());
		}

		//-- Generate the compound's where part.
		appendWhere("(");
		int ix = 0;
		for(String col : pm.getColumnNames()) {
			if(ix++ > 0)
				appendWhere(" and ");

			appendWhere(getColumnRef(m_root, col));
			appendOperation(n.getOperation());
			appendWhere("?");
		}
		appendWhere(")");

		int index = m_nextWhereIndex;
		IJdbcType tc = pm.getTypeConverter();
		m_nextWhereIndex += tc.columnCount();
		ValSetter vs = new ValSetter(index, inst, tc, pm);
		m_valList.add(vs);
	}

	/**
	 * Generate some comparison in where with a literal value. The literal must be conversion-compatible with
	 * the type converter for the property or sadness ensues.
	 * @param pm
	 * @param expr
	 */
	private void appendValueSetter(JdbcPropertyMeta pm, QLiteral expr) {
		appendWhere("?");
		int index = m_nextWhereIndex++;
		IJdbcType tc = pm.getTypeConverter();
		ValSetter vs = new ValSetter(index, expr.getValue(), tc, pm);
		m_valList.add(vs);
	}

	/**
	 * Append a value setter for a like operation, where the parameter is string by definition.
	 * @param pm
	 * @param expr
	 */
	private void appendLikeValueSetter(JdbcPropertyMeta pm, QLiteral expr) {
		if(!(expr.getValue() instanceof String))
			throw new QQuerySyntaxException("Invalid value type " + expr.getValue() + " for LIKE operation - expecting string.");
		appendWhere("?");
		int index = m_nextWhereIndex++;
		LikeSetter vs = new LikeSetter(index, (String) expr.getValue(), pm);
		m_valList.add(vs);
	}

	private JdbcPropertyMeta resolveProperty(String pname) throws Exception {
		if(pname.indexOf('.') != -1) {
			String[] segs = pname.split("\\.");
			JdbcClassMeta currclz = m_rootMeta;
			JdbcPropertyMeta selpm = null;
			int i = 0;
			for(;;) {
				String name = segs[i++];
				selpm = currclz.findProperty(name);
				if(selpm == null)
					throw new QQuerySyntaxException("Property '" + name + "' not found in class=" + currclz.getDataClass() + " in property path '" + pname + "'");

				if(i >= segs.length) // Done?
					return selpm;

				//-- There is another property. The current property MUST refer to a compound.
				if(!selpm.isCompound())
					throw new QQuerySyntaxException("Property '" + name + "' in class=" + currclz.getDataClass() + " in property path '" + pname + "' is not a compound property");
				currclz = JdbcMetaManager.getMeta(selpm.getActualClass());
			}
		}

		//-- Lookup,
		JdbcPropertyMeta pm = m_rootMeta.findProperty(pname);
		if(pm == null)
			throw new IllegalStateException(m_rootMeta.getDataClass() + "." + pname + ": unknown property");
		return pm;
	}

	@Override
	public void visitUnaryProperty(@Nonnull QUnaryProperty n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		if(n.getOperation() == QOperation.ISNOTNULL || n.getOperation() == QOperation.ISNULL) {
			JdbcPropertyMeta pm = resolveProperty(n.getProperty());
			appendWhere(getColumnRef(m_root, pm.getColumnName()));
			appendOperation(n.getOperation());
		} else {
			appendOperation(n.getOperation());
			appendWhere("(");
			JdbcPropertyMeta pm = resolveProperty(n.getProperty());
			appendWhere(getColumnRef(m_root, pm.getColumnName()));
			appendWhere(")");
		}

		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitBetween(@Nonnull QBetweenNode n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		//-- Lookup the property name. For now it cannot be dotted
		JdbcPropertyMeta pm = resolveProperty(n.getProp());
		appendWhere(getColumnRef(m_root, pm.getColumnName()));
		appendWhere(" between ");

		if(n.getA() instanceof QLiteral) {
			appendValueSetter(pm, (QLiteral) n.getA());
		} else
			throw new IllegalStateException("Unexpected argument to " + n + ": " + n.getA());

		appendWhere(" and ");
		if(n.getB() instanceof QLiteral) {
			appendValueSetter(pm, (QLiteral) n.getB());
		} else
			throw new IllegalStateException("Unexpected argument to " + n + ": " + n.getB());

		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitLiteral(@Nonnull QLiteral n) throws Exception {
		throw new IllegalStateException("!!! Trying to generate a naked literal!");
	}

	private void appendOperation(QOperation op) {
		appendOperation(renderOperation(op));
	}

	private void appendOperation(String renderOperation) {
		if(Character.isLetter(renderOperation.charAt(0))) {
			if(m_where.length() > 0 && m_where.charAt(m_where.length() - 1) != ' ')
				appendWhere(" ");
			appendWhere(renderOperation);
			appendWhere(" ");
		} else
			appendWhere(renderOperation);
	}

	static private String renderOperation(QOperation op) {
		switch(op){
			default:
				throw new IllegalStateException("Unexpected operation type=" + op);
			case AND:
				return "and";
			case OR:
				return "or";
			case NOT:
				return "not";
			case BETWEEN:
				return "between";
			case EQ:
				return "=";
			case NE:
				return "!=";
			case LT:
				return "<";
			case LE:
				return "<=";
			case GT:
				return ">";
			case GE:
				return ">=";
			case ILIKE:
				return "ilike";
			case LIKE:
				return "like";
			case ISNOTNULL:
				return "is not null";
			case ISNULL:
				return "is null";
			case SQL:
				return "SQL";
		}
	}

	/**
	 * Returns the operator precedence
	 * @param ot
	 * @return
	 */
	static public int getOperationPrecedence(final QOperation ot) {
		switch(ot){
			default:
				throw new IllegalStateException("Unknown operator " + ot);
			case OR:
				return 10;
			case AND:
				return 20;
			case NOT:
				return 25;
				/*case IN: */
			case BETWEEN:
			case LIKE:
			case ILIKE:
				return 30;

			case LT:
			case LE:
			case GT:
			case GE:
			case EQ:
			case NE:
			case ISNULL:
			case ISNOTNULL:
				return 40;
				//			case NOT:
				//				return 50;
				//				// ANY, ALL, SOME: 60
				//			case CONCAT:
				//				return 70;
				//			case PLUS: case MINUS:
				//				return 80;
				//			case MULT: case DIV: case MOD:
				//				return 90;
				//			case UMINUS:
				//				return 100;

			case LITERAL:
				return 100;
		}
	}

	@Override
	public void visitUnaryNode(final @Nonnull QUnaryNode n) throws Exception {
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
			case SQL:
				if(n.getNode() instanceof QLiteral) {
					QLiteral l = (QLiteral) n.getNode();
					appendWhere((String) l.getValue());
					return;
				}
				break;
			case NOT:
				if(n.getNode() == null) {
					return;
				}
				appendOperation(n.getOperation());
				int oldprec = m_curPrec;
				m_curPrec = getOperationPrecedence(n.getOperation());

				if(oldprec > m_curPrec){
					appendWhere("(");
				}

				n.getNode().visit(this);

				if(oldprec > m_curPrec){
					appendWhere(")");
				}

				m_curPrec = oldprec;
				return;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}

	@Override
	public void visitPropertyJoinComparison(@Nonnull QPropertyJoinComparison qPropertyJoinComparison) {
		throw new IllegalStateException("Correlated subqueries are not supported");
	}

	@Deprecated
	@Override
	public void visitSelectionSubquery(QSelectionSubquery qSelectionSubquery) throws Exception {
		throw new IllegalStateException("Subqueries are not supported");
	}
}
