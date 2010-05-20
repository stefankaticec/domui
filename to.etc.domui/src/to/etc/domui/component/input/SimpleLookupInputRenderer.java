package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * This renderer represents default renderer that is used for {@link LookupInput} control.
 * It can be additionaly customized (before and after custom content) by setting provided {@link ICustomContentFactory} fields.
 * See {@link SimpleLookupInputRenderer#setBeforeContent} and {@link SimpleLookupInputRenderer#setAfterContent}.
 * Custom added content would be enveloped into separate row(s).
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 10, 2010
 */
public class SimpleLookupInputRenderer<T> implements INodeContentRenderer<T> {

	public SimpleLookupInputRenderer() {}

	private INodeContentRenderer<T> m_beforeRenderer;

	private INodeContentRenderer<T> m_afterRenderer;

	public void renderNodeContent(NodeBase component, NodeContainer node, T object, Object parameters) throws Exception {
		String txt;
		TBody tbl = ((Table) node).getBody();
		if(getBeforeRenderer() != null) {
			TD cell = new TD();
			getBeforeRenderer().renderNodeContent(component, cell, object, parameters);
			if(cell.getChildCount() != 0)
				tbl.addRow().add(cell);
		}

		if(object != null) {
			ClassMetaModel cmm = MetaManager.findClassMeta(object.getClass());
			if(cmm != null) {
				//-- Has default meta?
				List<DisplayPropertyMetaModel> l = cmm.getTableDisplayProperties();
				if(l.size() == 0)
					l = cmm.getComboDisplayProperties();
				if(l.size() > 0) {
					//-- Expand the thingy: render a single line separated with BRs
					List<ExpandedDisplayProperty> xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
					xpl = ExpandedDisplayProperty.flatten(xpl);
						//						node.add(tbl);
					tbl.setCssClass("ui-lui-v");
					int c = 0;
					int mw = 0;
					for(ExpandedDisplayProperty xp : xpl) {
						String val = xp.getPresentationString(object);
						if(val == null || val.length() == 0)
							continue;
						TR tr = new TR();
						tbl.add(tr);
						TD td = new TD(); // Value thingy.
						tr.add(td);
						td.setCssClass("ui-lui-vcell");
						td.setValign(TableVAlign.TOP);
						td.add(val);
						int len = val.length();
						if(len > mw)
							mw = len;
							td = new TD();
						tr.add(td);
						td.setValign(TableVAlign.TOP);
						td.setCssClass("ui-lui-btncell");
						td.setWidth("1%");
						if(c++ == 0 && parameters != null) {
							td.add((NodeBase) parameters); // Add the button,
						}
					}
					mw += 4;
					if(mw > 40)
						mw = 40;
					tbl.setWidth(mw + "em");
					return;
				}
			}
			txt = object.toString();
		} else
			txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
		TR r = new TR();
		tbl.add(r);
		TD td = new TD();
		r.add(td);
		td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-v");
		td.add(txt);
			//-- parameters is either the button, or null if this is a readonly version.
		if(parameters != null) {
			td = new TD();
			r.add(td);
			td.setValign(TableVAlign.TOP);
			td.setWidth("1%");
			td.add((NodeBase) parameters); // Add the button,
		}

		if(getAfterRenderer() != null) {
			TD cell = new TD();
			getAfterRenderer().renderNodeContent(component, cell, object, parameters);
			if(cell.getChildCount() != 0)
				tbl.addRow().add(cell);
		}
	}

	/**
	 * Enables inserting of custom content that would be enveloped into additionaly added row that is inserted before rows that are part of builtin content.
	 */
	public INodeContentRenderer<T> getBeforeRenderer() {
		return m_beforeRenderer;
	}

	/**
	 * Enables inserting of custom content that would be enveloped into additionaly added row that is inserted before rows that are part of builtin content.
	 * @param afterContent
	 */
	public void setBeforeRenderer(INodeContentRenderer<T> beforeContent) {
		m_beforeRenderer = beforeContent;
	}

	/**
	 * Enables appending of custom content that would be enveloped into additionaly added row <i>after</i> the actual data.
	 */
	public INodeContentRenderer<T> getAfterRenderer() {
		return m_afterRenderer;
	}

	/**
	 * Enables appending of custom content that would be enveloped into additionaly added row <i>after</i> the actual data.
	 * @param afterContent
	 */
	public void setAfterRenderer(INodeContentRenderer<T> afterContent) {
		m_afterRenderer = afterContent;
	}
}