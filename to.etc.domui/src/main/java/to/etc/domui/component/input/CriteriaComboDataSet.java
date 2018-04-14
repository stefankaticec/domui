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
package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.IComboDataSet;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * A Combobox dataset provider which creates a dataset by using a QCriteria passed to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2010
 */
public class CriteriaComboDataSet<T> implements IComboDataSet<T> {
	@NonNull
	final private QCriteria<T> m_query;

	/**
	 * Create with the specified immutable QCriteria.
	 * @param query
	 */
	public CriteriaComboDataSet(@NonNull QCriteria<T> query) {
		m_query = query;
	}

	/**
	 * Execute the query and return the result.
	 * @see to.etc.domui.util.IComboDataSet#getComboDataSet(to.etc.domui.dom.html.UrlPage)
	 */
	@Override
	public @NonNull List<T> getComboDataSet(@NonNull UrlPage page) throws Exception {
		return page.getSharedContext().query(m_query);
	}
}
