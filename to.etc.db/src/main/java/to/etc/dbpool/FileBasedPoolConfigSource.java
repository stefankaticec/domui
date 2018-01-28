package to.etc.dbpool;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-1-18.
 */
abstract public class FileBasedPoolConfigSource extends PoolConfigSource {
	private File m_src;

	private File m_backupSrc;

	public FileBasedPoolConfigSource(File src, File back) {
		m_src = src;
		m_backupSrc = back;
	}

	public File getBackupSrc() {
		return m_backupSrc;
	}

	public File getSrc() {
		return m_src;
	}

	@Override
	public String toString() {
		if(m_backupSrc != null)
			return m_src + " (" + m_backupSrc + ")";
		if(m_src == null)
			return "(parameters)";
		return m_src.toString();
	}

}
