package to.etc.vera.db

import org.hibernate.annotations.GenericGenerator
import to.etc.dbpool.PoolManager
import to.etc.domui.hibernate.config.HibernateConfigurator
import to.etc.domui.hibernate.idgen.UUIDGenerator23
import to.etc.webapp.query.QDataContextFactory
import java.io.File
import javax.persistence.*
import javax.sql.DataSource

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */

@MappedSuperclass
open class DbAbstractBase {
	var id : String? = null
		@Column(name = "id", length = 23, nullable = false)
		@Id() @GeneratedValue(generator = "uuid3") @GenericGenerator(name="uuid3", strategy = UUIDGenerator23.NAME)
		get

}

@Entity
@Table(name = "vra_domain")
class DbVeraDomain : DbAbstractBase() {
	var name : String? = null
		@Column(name = "dom_name", length = 128, nullable = false)
		get

	var rgbColor: String? = null
		@Column(name = "dom_color", length = 6, nullable = false)
		get

	var mnemonic: String? = null
		@Column(name = "dom_mnemonic", length = 3, nullable = false)
		get
}

@Entity
@Table(name = "vra_class")
class DbVeraClass : DbAbstractBase() {
	var name : String? = null
		@Column(name = "cls_name", length = 128, nullable = false)
		get

	var domain: DbVeraDomain? = null
		@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "dom_id")
		get
}

@Entity
@Table(name = "vra_attribute")
class DbVeraAttribute : DbAbstractBase() {
	var name : String? = null
		@Column(name = "att_name", length = 128, nullable = false)
		get

	var veraClass: DbVeraClass? = null
		@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cls_id")
		get
}

object DbUtil {
	fun initialize(ds: DataSource) : Unit {
		HibernateConfigurator.addClasses(DbVeraAttribute::class.java, DbVeraClass::class.java, DbVeraDomain::class.java)
		HibernateConfigurator.schemaUpdate(HibernateConfigurator.Mode.UPDATE)
		HibernateConfigurator.initialize(ds)
	}

	fun getContextSource(): QDataContextFactory {
		return HibernateConfigurator.getDataContextFactory()
	}

	/**
	 * Initialize the layer using a poolid in the specified poolfile, except when there is no
	 * db pool defined there; in that case use a temp database.
	 */
	fun initialize(poolfile: File, poolname: String) {
		val pool = PoolManager.getInstance().definePool(poolfile, poolname)
		initialize(pool.getPooledDataSource())
	}

}
