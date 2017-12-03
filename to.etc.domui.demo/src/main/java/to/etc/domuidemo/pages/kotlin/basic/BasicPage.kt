package to.etc.domuidemo.pages.kotlin.basic

import to.etc.domui.dom.html.Span
import to.etc.domui.dom.html.UrlPage
import to.etc.webapp.query.QCriteria
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-11-17.
 */
class KtPage : UrlPage() {
	var myName : String = ""


	override fun createContent() {
		add(Span("Hello, world"))

//		val q = QCriteria.create(Artist::class.java)
////				.eq(Artist::getName, 123.34)
//				.eq(Artist::getId, 123)
//
//		val kFunction1: KFunction1<Artist, String> = Artist::getName
//
//		val name2: KProperty1<KtPage, String> = KtPage::myName
//
//		QCriteria.create(KtPage::class.java)
//				.eq(KtPage::myName, 123)
//
//		testValue(123, "adsd")
//
//		testJavaClass(String::class.java, "123")
//		testKClass(String::class, 123)
//
//		val prop = Prop<String>("Hi")
//		test2(prop, "123")
//		test2(prop, 456)
//
//		val prop2 = Prop2(Artist(), "Hello")
//		test3(prop2, 12)
//
		eq(KtPage::myName, 123)

	}
//
//	private fun <V> testValue(a: V, b: V) {
//
//	}
//
//	private fun <V> test2(a: Prop<V>, b: V) {
//	}
//	private fun <T, V> test3(a: Prop2<T, V>, b: V) {
//	}
//
//
//	private fun <V: Any> testKClass(clz: KClass<V>, value : V) {
//
//	}
//	private fun <V> testJavaClass(clz: Class<V>, value : V) {
//
//	}
//
	fun <T, V> eq(property: KProperty1<T, V>, value: V) {

	}
}

private fun <T, V : Any> QCriteria<T>.eq(kProperty1: KProperty1<T, V>, value: V) {
}

private fun <T, V : Any> QCriteria<T>.eq(getter: KFunction1<T, V>, value: V): QCriteria<T> {
	val name = getter.name
	println("Name is " + name + ", value = " + value)

	return this

}

class Prop<T>(v : T) {
	val value : T = v
}

class Prop2<T, out V>(v : T, x: V) {
	val clz : T = v
	val cv : V = x;
}

