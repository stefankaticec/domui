package to.etc.domuidemo.pages.kotlin.basic

import to.etc.domui.derbydata.db.Artist
import to.etc.domui.dom.html.Span
import to.etc.domui.dom.html.UrlPage
import to.etc.webapp.query.QCriteria
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-11-17.
 */
class KtPage : UrlPage() {
	val myName : String = ""


	override fun createContent() {
		add(Span("Hello, world"))

		val q = QCriteria.create(Artist::class.java)
//				.eq(Artist::getName, 123.34)
				.eq(Artist::getId, 123)

		val kFunction1: KFunction1<Artist, String> = Artist::getName

		val name2: KProperty1<KtPage, String> = KtPage::myName

		QCriteria.create(KtPage::class.java)
				.eq(KtPage::myName, 123)

		testValue(123, "adsd")

		testJavaClass(String::class.java, "123")
		testKClass(String::class, "123")
	}

	private fun <V> testValue(a: V, b: V) {

	}

	private fun <V: Any> testKClass(clz: KClass<V>, value : V) {

	}
	private fun <V> testJavaClass(clz: Class<V>, value : V) {

	}
}

private fun <T, V : Any> QCriteria<T>.eq(kProperty1: KProperty1<T, V>, value: V) {
}

private fun <T, V : Any> QCriteria<T>.eq(getter: KFunction1<T, V>, value: V): QCriteria<T> {
	val name = getter.name
	println("Name is " + name + ", value = " + value)

	return this

}


