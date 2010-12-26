package to.etc.domui.component.tree;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A popup window that shows a tree and lets the user select one entry from it. It shows a tree with
 * the specified model, user controllable node content renderer. It adds a button bar with select and
 * cancel buttons, and has setClicked() and setCancelClicked() handlers to handle selection events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 10, 2010
 */
public class TreeSelectionWindow<T> extends FloatingWindow implements ICellClicked<T> {
	private Tree<T> m_tree;

	private T	m_selected;

	@Nonnull
	private ITreeModel<T>	m_model;

	private NodeBase		m_selectedNode;

	private long m_lastClickTS;

	private IClicked< ? > m_clicked;

	private IClicked< ? > m_cancelClicked;

	private INodeContentRenderer<T> m_contentRenderer;

	public TreeSelectionWindow(boolean modal, String txt, @Nonnull ITreeModel<T> model) {
		super(modal, txt);
		m_model = model;
		setOnClose(new IClicked<FloatingWindow>() {

			@Override
			public void clicked(FloatingWindow clickednode) throws Exception {
				cancel();
			}
		});
	}

	@Override
	public void createContent() throws Exception {
		setWidth("400px");
		super.createContent();
		ButtonBar	bb = new ButtonBar();
		add(bb);
		bb.addButton(Msgs.BUNDLE.getString("ui.tsw.select"), new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				select();
			}
		});
		bb.addButton(Msgs.BUNDLE.getString("ui.tsw.cancel"), new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				cancel();
			}
		});
		Div	fixed = new Div();
		add(fixed);
		fixed.setHeight("300px");
		fixed.setOverflow(Overflow.AUTO);
		m_tree = new Tree<T>();
		fixed.add(m_tree);
		m_tree.setModel(m_model);
		m_tree.setContentRenderer(m_contentRenderer);
		m_tree.setCellClicked(this);
	}

	@SuppressWarnings("rawtypes")
	protected void cancel() throws Exception {
		close();
		m_selected = null;
		m_selectedNode = null;

		if(getCancelClicked() != null) {
			((IClicked) getCancelClicked()).clicked(this);
			return;
		}
		if(getClicked() != null) {
			((IClicked) getClicked()).clicked(this);
		}
	}

	@SuppressWarnings("rawtypes")
	protected void select() throws Exception {
		if(m_selected == null)
			return;
		close();
		if(getClicked() != null) {
			((IClicked) getClicked()).clicked(this);
		}
	}

	/**
	 * Internally called when tree node is clicked. Handles selection events.
	 * @see to.etc.domui.component.tbl.ICellClicked#cellClicked(to.etc.domui.dom.html.Page, to.etc.domui.dom.html.NodeBase, java.lang.Object)
	 */
	@Override
	final public void cellClicked(Page pg, NodeBase tr, T rowval) throws Exception {
		long ts = System.currentTimeMillis();
		if(m_selected == rowval) {
			//-- Reselect...
			long dt = ts - m_lastClickTS;
			if(dt < 500) {
				//-- Treat double click as select
				select();
				return;
			}
		}

		if(m_selectedNode != null) {
			m_selectedNode.removeCssClass("selected");
			m_selected = null;
			m_selectedNode = null;
		}
		m_lastClickTS = ts;
		m_selectedNode = tr;
		m_selected = rowval;
		tr.addCssClass("selected");


	}

	/**
	 * Inhibit a Javascript clicked handler so we can override the clicked handler for selection.
	 * @return
	 */
	@Override
	public boolean internalNeedClickHandler() {
		return false;
	}

	public T getSelected() {
		return m_selected;
	}

	public void setSelected(T selected) {
		m_selected = selected;
	}

	@Override
	public IClicked< ? > getClicked() {
		return m_clicked;
	}

	@Override
	public void setClicked(IClicked< ? > clicked) {
		m_clicked = clicked;
	}

	public IClicked< ? > getCancelClicked() {
		return m_cancelClicked;
	}

	public void setCancelClicked(IClicked< ? > cancelClicked) {
		m_cancelClicked = cancelClicked;
	}

	public INodeContentRenderer<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}
}