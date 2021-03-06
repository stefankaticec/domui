/*
 * DomUI Java User Interface - shared code
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
package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

public class NdConvert extends NdUnary {
	private Class m_tocl;

	public NdConvert(NdBase b, Class tocl) {
		super(b);
		m_tocl = tocl;
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_expr.evaluate(vr);
		return RuntimeConversions.convertTo(a, m_tocl);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append("(cast-to ");
		a.append(m_tocl.getName());
		a.append(")");
	}
}
