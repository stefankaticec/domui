package to.etc.domui.kotlin.ui

import to.etc.domui.component.layout.TabPanel
import to.etc.domui.dom.html.Div
import to.etc.domui.dom.html.NodeContainer
import to.etc.domui.dom.html.Span

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-1-18.
 */
fun NodeContainer.div(css: String  ="", init: NodeContainer.() -> Unit): Div {
	val d = Div()
	add(d)
	if(css.isNotEmpty())
		d.addCssClass(css)
	d.init()
	return d
}

fun NodeContainer.span(text: String="", init: Span.() -> Unit = {}): Span {
	val d = Span()
	add(d)
	if(text.isNotEmpty())
		d.add(text)
	d.init()
	return d
}

fun NodeContainer.tabPanel(init: TabPanel.() -> Unit) : TabPanel {
	val t = TabPanel()
	add(t)
	t.init()
	return t
}

