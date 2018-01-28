package to.etc.dbpool;

import java.util.Properties;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-1-18.
 */
public class PropertiesSource extends PoolConfigSource {
	final private Properties m_properties;

	public PropertiesSource(Properties properties) {
		m_properties = properties;
	}

	@Override
	public String getProperty(String section, String name) throws Exception {
		String key = section + "." + name;
		return m_properties.getProperty(key);
	}
}
