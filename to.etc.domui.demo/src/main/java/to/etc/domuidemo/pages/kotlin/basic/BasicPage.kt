package to.etc.domuidemo.pages.kotlin.basic

import to.etc.domui.dom.html.Span
import to.etc.domui.dom.html.UrlPage

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-11-17.
 */
class KtPage : UrlPage() {
	override fun createContent() {
		add(Span("Hello, world"))

	}
}
