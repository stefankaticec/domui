package to.etc.vera

import to.etc.dbpool.ConnectionPool
import to.etc.dbpool.PoolManager
import to.etc.domui.dom.html.UrlPage
import to.etc.domui.hibernate.model.HibernateModelCopier
import to.etc.domui.server.ConfigParameters
import to.etc.domui.server.DomApplication
import to.etc.domui.util.db.QCopy
import to.etc.util.DeveloperOptions
import to.etc.vera.db.DbUtil
import to.etc.webapp.query.QContextManager
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * @author [Frits Jalvingh](mailto:jal@etc.to)
 * Created on 21-1-18.
 */
class VeraApplication : DomApplication() {
	private val DUTCH = Locale("nl", "NL")

	@Throws(Exception::class)
	override fun initialize(pp: ConfigParameters) {
		super.initialize(pp)

		val propertyFile = getPropertyFile()
		val properties = getProperties(propertyFile)

		val appUrl = properties.getProperty("application.url")            // Deep, deep sigh.
		if(null != appUrl)
			setApplicationURL(appUrl)
		initializeDatabase(propertyFile, properties)


	}

	private fun initializeDatabase(propertyFile: File, properties: Properties) {
		val driver = properties.getProperty("vera.driver")
		if(null != driver) {
			DbUtil.initialize(propertyFile, "vera")
		} else {
			val pool = createTempDatabase()
			DbUtil.initialize(pool.pooledDataSource)
		}

		QContextManager.setImplementation(QContextManager.DEFAULT, DbUtil.getContextSource()) // Prime factory with connection source
		QCopy.setImplementation(HibernateModelCopier())
	}

	private fun createTempDatabase(): ConnectionPool {
		var path = "/tmp/veraDB/data"
		if(System.getProperty("maven.home") != null || System.getProperty("failsafe.test.class.path") != null) {
			val tmp = File.createTempFile("testdb", ".vera")
			tmp.delete()
			tmp.mkdirs()
			path = tmp.absolutePath
		} else {
			val root = File(path)
			root.parentFile.mkdirs()
		}
		println("Database path is " + path)

		val pool = PoolManager.getInstance().definePool(
			"vera", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + path, "sa", "", null
		)
		pool!!.initialize()
		return pool
	}

	/**
	 * Default to the Dutch locale for all requests, despite the browser's locale.
	 */
	override fun getRequestLocale(request: HttpServletRequest): Locale {
		return DUTCH
	}

	@Throws(Exception::class)
	private fun getProperties(propertyFile: File): Properties {

		val p = Properties()
		FileInputStream(propertyFile).use { `is` -> p.load(`is`) }
		return p
	}

	private fun getPropertyFile(): File {
		var pf: File
		var name: String? = System.getProperty("config")
		if(null != name) {
			if(name.startsWith(File.separator)) {
				pf = File(name)
				if(pf.exists())
					return pf
				//-- Abort: given name is wrong
			} else {
				//-- Relative name, try home 1st
				val home = System.getProperty("user.home")
				pf = File(home + File.separator + name)
				if(pf.exists())
					return pf
				pf = getAppFile("WEB-INF/" + name)
				if(pf.exists())
					return pf
			}

			// File requested does not exist.
			throw IOException("The config file $pf does not exist")
		} else {
			//-- Use the default name and paths
			val dfName = System.getProperty("user.home") + "/.vera/vera.properties"
			name = DeveloperOptions.getString("vera.config", dfName)
			pf = File(name)
			if(pf.exists())
				return pf

			pf = getAppFile("WEB-INF/vera.properties")
			if(pf.exists())
				return pf
			throw IllegalStateException("The config file $pf does not exist")
		}
	}

	override fun getRootPage(): Class<out UrlPage>? {
		return Index::class.java
	}
}
