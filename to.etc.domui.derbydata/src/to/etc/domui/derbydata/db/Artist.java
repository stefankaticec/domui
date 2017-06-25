package to.etc.domui.derbydata.db;

import org.hibernate.annotations.Index;
import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.MetaObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Artist")
@SequenceGenerator(name = "sq", sequenceName = "artist_sq")
@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")})
public class Artist extends DbRecordBase<Long> {
	private Long m_id;

	private String m_name;

	private List<Album> m_albumList = new ArrayList<Album>();

	@Override
	@Id
	@SequenceGenerator(name = "sq", sequenceName = "artist_sq")
	@Column(name = "ArtistId", nullable = false, precision = 20)
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	@Column(length = 120, nullable = false, unique = true)
	@Index(name = "Name")
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@OneToMany(mappedBy = "artist")
	public List<Album> getAlbumList() {
		return m_albumList;
	}

	public void setAlbumList(List<Album> albumList) {
		m_albumList = albumList;
	}
}