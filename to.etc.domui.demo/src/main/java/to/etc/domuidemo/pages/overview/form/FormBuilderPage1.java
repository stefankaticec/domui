package to.etc.domuidemo.pages.overview.form;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.UrlPage;

import java.util.ArrayList;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-3-19.
 */
public class FormBuilderPage1 extends UrlPage {
	private Album m_album;

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		FormBuilder fb = new FormBuilder(cp);
		ComboLookup2<Artist> artist = fb.property(m_album, Album_.artist()).control(ComboLookup2.class);
		artist.setData(new ArrayList<>());
	}

	public Album getAlbum() {
		return m_album;
	}

	public void setAlbum(Album album) {
		m_album = album;
	}
}
