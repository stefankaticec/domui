package to.etc.vera.pages.crud

import to.etc.domui.component.tbl.DataTable
import to.etc.domui.component.tbl.RowRenderer
import to.etc.domui.component.tbl.SimpleSearchModel
import to.etc.domui.dom.html.UrlPage
import to.etc.vera.db.DbVeraDomain
import to.etc.webapp.query.QCriteria

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-1-18.
 */
class DomainListPage : UrlPage() {
	override fun createContent() {
		val rr = RowRenderer(DbVeraDomain::class.java)
		rr.column(DbVeraDomain::name).label("Naam").width(30)
		rr.column(DbVeraDomain::mnemonic).label("Mnemonic").width(10)
		rr.column(DbVeraDomain::rgbColor).label("Presentatiekleur").width(10)

		val qm = SimpleSearchModel(this, QCriteria.create(DbVeraDomain::class.java))
		val dt = DataTable(qm, rr)
		add(dt)
	}
}

class DomainEditPage: UrlPage() {
	override fun createContent() {
		val q = QCriteria.create()


	}

}
