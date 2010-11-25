package to.etc.util;

/**
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 22, 2005
 */
@Deprecated
public class ConfigSourceWrapper {
	private ConfigSource	m_s;

	public ConfigSourceWrapper(ConfigSource s) {
		m_s = s;
	}

	public ConfigSource getConfigSource() {
		return m_s;
	}

	public String getOption(String key) throws Exception {
		String v = m_s.getOption(key);
		return v;
	}

	public String getOption(String key, String dflt) throws Exception {
		String v = m_s.getOption(key);
		return v == null ? dflt : v;
	}

	public int getInt(String key) throws Exception {
		String v = m_s.getOption(key);
		if(v == null)
			throw new IllegalStateException("Missing property '" + key + "' in " + m_s.getSourceObject());
		try {
			return Integer.parseInt(v.trim());
		} catch(Exception x) {}
		throw new IllegalStateException("Bad integer value for property '" + key + "' in " + m_s.getSourceObject() + ": " + v);
	}

	public int getInt(String key, int dflt) throws Exception {
		String v = m_s.getOption(key);
		if(v == null)
			return dflt;
		v = v.trim();
		if(v.length() == 0)
			return dflt;
		try {
			return Integer.parseInt(v.trim());
		} catch(Exception x) {}
		throw new IllegalStateException("Bad integer value for property '" + key + "' in " + m_s.getSourceObject() + ": " + v);
	}

	public boolean getBool(String key) throws Exception {
		String v = m_s.getOption(key);
		if(v == null)
			throw new IllegalStateException("Missing boolean property '" + key + "' in " + m_s.getSourceObject());
		v = v.trim().toUpperCase();
		if(v.length() == 0)
			return true;
		if(v.equals("ON"))
			return true;
		else if(v.equals("OFF"))
			return false;
		char c = v.charAt(0);
		return c == 'T' || c == 'Y' || c == '1';
	}

	public boolean getBool(String key, boolean dflt) throws Exception {
		String v = m_s.getOption(key);
		if(v == null)
			return dflt;
		return getBool(key);
	}

	/**
	 * Gets a time value from the parameters. The time has the format HHMM or
	 * HH:MM, and is in 24h notation. The time is returned as a number
	 * representing the #minutes since 0:00h.
	 */
	public int getTime(String propertyname, String defaultval) throws Exception {
		String v = getOption(propertyname, defaultval);

		//-- Now convert the date...
		String hs, ms;
		if(v.length() == 4) {
			hs = v.substring(0, 2);
			ms = v.substring(2, 4);
		} else if(v.length() == 5) {
			hs = v.substring(0, 2);
			ms = v.substring(3, 5);
			if(v.charAt(2) != ':')
				throw new Exception(v + ": invalid time value, must be HH:MM");
		} else
			throw new Exception(v + ": invalid time value, must be HH:MM");

		//-- Convert #s
		int h, m;
		try {
			h = Integer.parseInt(hs);
			m = Integer.parseInt(ms);
		} catch(Exception x) {
			throw new Exception(v + ": must be hh:mm or hhmm");
		}

		if(h < 0 || h > 23 || m < 0 || m > 60)
			throw new Exception(v + ": invalid time (hours or minutes bad)");
		return h * 60 + m;
	}


	@Override
	public String toString() {
		return m_s.getSourceObject().toString();
	}
}
