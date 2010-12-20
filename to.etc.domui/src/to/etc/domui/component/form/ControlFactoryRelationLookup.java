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
package to.etc.domui.component.form;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Accepts any UP (parent) relation and scores 3, preferring this above the combobox-based
 * lookup.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryRelationLookup implements ControlFactory {
	/**
	 * Accept any UP relation.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		if(controlClass != null && !controlClass.isAssignableFrom(LookupInput.class))
			return -1;

		if(pmm.getRelationType() != PropertyRelationType.UP)
			return 0;
		if(Constants.COMPONENT_LOOKUP.equals(pmm.getComponentTypeHint()))
			return 10;
		return 3; // Prefer a lookup above a combo if unspecified
	}

	/**
	 * Create the lookup thingy.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public ControlFactoryResult createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		//-- We'll do a lookup thingy for sure.
		LookupInput<Object> li = new LookupInput<Object>((Class<Object>) pmm.getActualType(), pmm.getClassModel());
		li.setReadOnly(!editable);

		//-- 1. Define search fields from property, then class.lookup, then generic
		List<SearchPropertyMetaModel> sp = pmm.getLookupFieldSearchProperties();		// Property override?
		if(sp.size() == 0) {
			sp = li.getMetaModel().getLookupFieldSearchProperties();	// Class level?
			if(sp.size() == 0) {
				sp = li.getMetaModel().getSearchProperties();		// Generic table properties.
				if(sp.size() > 0)
					li.setSearchProperties(sp);
			}
		}

		//-- 2. Define keyword search properties in the same way.


		if(pmm.getLookupFieldRenderer() != null)
			li.setContentRenderer((INodeContentRenderer<Object>) DomApplication.get().createInstance(pmm.getLookupFieldRenderer())); // Bloody stupid Java generic crap
		else {
			ClassMetaModel cmm = MetaManager.findClassMeta(pmm.getActualType()); // Get meta for type reached,
			if(cmm.getLookupFieldRenderer() != null)
				li.setContentRenderer((INodeContentRenderer<Object>) DomApplication.get().createInstance(cmm.getLookupFieldRenderer())); // Bloody stupid Java generic crap
		}
		if(pmm.isRequired())
			li.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			li.setTitle(s);
		return new ControlFactoryResult(li, model, pmm);
	}
}

