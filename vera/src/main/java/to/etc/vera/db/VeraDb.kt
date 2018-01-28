package to.etc.vera.db

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.engine.SessionImplementor
import org.hibernate.id.UUIDGenerationStrategy
import to.etc.dbpool.PoolManager
import to.etc.domui.hibernate.config.HibernateConfigurator
import to.etc.webapp.query.QDataContextFactory
import java.io.File
import java.util.*
import javax.persistence.*
import javax.sql.DataSource

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */

/**
 * This ID generator creates an UUID as the ID.
 */
class UUIDStrategy : UUIDGenerationStrategy {
	override fun getGeneratedVersion(): Int {
		return 4
	}

	override fun generateUUID(p0: SessionImplementor?): UUID {
		return UUID.randomUUID()
//		val bytes = ByteArray(16)
//		moveBytes(bytes, 0, id.mostSignificantBits)
//		moveBytes(bytes, 8, id.leastSignificantBits)
//		val str = StringTool.encodeBase64(bytes)
	}

	private fun moveBytes(bytes: ByteArray, offset: Int, bits: Long) {
		var dumbCopy = bits
		for(i in offset+8 downTo offset) {
			bytes[i] = (dumbCopy and 0xff).toByte()
			dumbCopy = dumbCopy shr 8
		}
	}
}

@Entity
@Table(name = "vra_domain")
class DbVeraDomain {
	var id : String? = null
		@Column(name = "dom_id", length = 23, nullable = false)
		@Id() @GeneratedValue(generator = "uuid2") @GenericGenerator(name="uuid2", strategy = "uuid2", parameters = [Parameter(name = "uuid_gen_strategy_class", value = "UuidStrategy")])
		get

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
class DbVeraClass {
	var id : String? = null
		@Column(name = "cls_id", length = 23, nullable = false)
		@Id() @GeneratedValue(generator = "uuid2") @GenericGenerator(name="uuid2", strategy = "uuid2", parameters = [Parameter(name = "uuid_gen_strategy_class", value = "UuidStrategy")])
		get

	var name : String? = null
		@Column(name = "cls_name", length = 128, nullable = false)
		get

	var domain: DbVeraDomain? = null
		@ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "dom_id")
		get
}

@Entity
@Table(name = "vra_attribute")
class DbVeraAttribute {
	var id : String? = null
		@Column(name = "att_id", length = 23, nullable = false)
		@Id() @GeneratedValue(generator = "uuid2") @GenericGenerator(name="uuid2", strategy = "uuid2", parameters = [Parameter(name = "uuid_gen_strategy_class", value = "UuidStrategy")])
		get

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
