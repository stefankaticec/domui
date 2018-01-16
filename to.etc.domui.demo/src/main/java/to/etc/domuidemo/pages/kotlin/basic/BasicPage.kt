package to.etc.domuidemo.pages.kotlin.basic

import to.etc.domui.dom.html.UrlPage
import to.etc.domui.kotlin.ui.div
import to.etc.domui.kotlin.ui.span
import to.etc.domui.kotlin.ui.tabPanel

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-11-17.
 */
class KtPage : UrlPage() {
	var myName: String = ""


	override fun createContent() {
		div(css="kt-test") {
			span(text="Hello, world")

			tabPanel {

			}

		}
	}
}
