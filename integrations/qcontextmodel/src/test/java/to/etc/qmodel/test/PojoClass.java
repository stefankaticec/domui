package to.etc.qmodel.test;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-2-19.
 */
public class PojoClass {
	private String m_name;

	private int m_count;

	private List<String> m_members;

	public PojoClass() {
	}

	public PojoClass(String name, int count, List<String> members) {
		m_name = name;
		m_count = count;
		m_members = members;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public int getCount() {
		return m_count;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public List<String> getMembers() {
		return m_members;
	}

	public void setMembers(List<String> members) {
		m_members = members;
	}
}
