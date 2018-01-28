package to.etc.vera

import to.etc.domui.dom.html.UrlPage
import to.etc.domui.kotlin.ui.div
import to.etc.domui.kotlin.ui.span

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
class Index : UrlPage() {
	override fun createContent() {
		div {
			span(text = "Hello, world")
		}
	}
}
