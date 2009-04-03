package to.etc.domui.components.menu;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.nls.*;
import to.etc.domui.utils.*;

/**
 * A single item in the menu, as defined by the *code*.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuItemImpl implements IMenuItem {
	private String m_id;

	private BundleRef m_msgBundle;

	private String m_labelKey;

	private String	m_titleKey;

	private String m_descKey;

	private String m_searchKey;

	private String m_parentID;

	private Class< ? extends UrlPage> m_pageClass;

	private PageParameters m_pageParameters;

	private String m_iconPath;

	private boolean m_disabled;

	private int	m_order;

	/** When adding this can be set to define the child's parent. If unset other items are checked to find a parent menu. */
	private MenuItemImpl		m_parent;

	/** The list of rights the user MUST have to access this menu item. This can get delegated to the UrlPage's annotation. */
	private Right[]		m_requiredRights;

	private List<IMenuItem> m_children = new ArrayList<IMenuItem>();

	public MenuItemImpl() {
	}

	/**
	 * Defines the rights that the user MUST have to see this menu item. If more than one Right is passed the user needs to possess *all* rights. When
	 * defined this overrides the rights in
	 * @param name
	 * @return
	 */
	public IMenuItem		setRequiredRights(final Right... rights) {
		m_requiredRights = rights;
		return this;
	}
	public IMenuItem		setImage(final String name) {
		m_iconPath = name;
		return this;
	}
	public IMenuItem		setImage(final Class<?> res, final String name) {
		m_iconPath = DomUtil.getJavaResourceRURL(res, name);
		return this;
	}

	private String	byKey(final String k) {
		Locale loc = NlsContext.getLocale();
		return m_msgBundle.getString(loc, k);
	}

	/**
	 * @see to.etc.domui.components.menu.IMenuItem#getId()
	 */
	public String getId() {
		return m_id;
	}
	public void setId(final String id) {
		m_id = id;
	}
	public BundleRef getMsgBundle() {
		return m_msgBundle;
	}
	public void setMsgBundle(final BundleRef msgBundle) {
		m_msgBundle = msgBundle;
	}
	public String getLabelKey() {
		return m_labelKey;
	}
	public void setLabelKey(final String labelKey) {
		m_labelKey = labelKey;
	}
	public String getDescKey() {
		return m_descKey;
	}
	public void setDescKey(final String descKey) {
		m_descKey = descKey;
	}
	public String getSearchKey() {
		return m_searchKey;
	}
	public void setSearchKey(final String searchKey) {
		m_searchKey = searchKey;
	}
	/**
	 * @see to.etc.domui.components.menu.IMenuItem#getParentID()
	 */
	public String getParentID() {
		return m_parentID;
	}
	public void setParentID(final String parentID) {
		m_parentID = parentID;
	}
	/**
	 * @see to.etc.domui.components.menu.IMenuItem#getPageClass()
	 */
	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}
	public MenuItemImpl setPageClass(final Class< ? extends UrlPage> pageClass) {
		m_pageClass = pageClass;
		return this;
	}
	/**
	 * @see to.etc.domui.components.menu.IMenuItem#getPageParameters()
	 */
	public PageParameters getPageParameters() {
		return m_pageParameters;
	}
	public MenuItemImpl setPageParameters(final PageParameters pageParameters) {
		m_pageParameters = pageParameters;
		return this;
	}
	/**
	 * @see to.etc.domui.components.menu.IMenuItem#getIconPath()
	 */
	public String getIconPath() {
		return m_iconPath;
	}
	public void setIconPath(final String iconPath) {
		m_iconPath = iconPath;
	}
	/**
	 * @see to.etc.domui.components.menu.IMenuItem#isDisabled()
	 */
	public boolean isDisabled() {
		return m_disabled;
	}
	public void setDisabled(final boolean disabled) {
		m_disabled = disabled;
	}
	/**
	 * @see to.etc.domui.components.menu.IMenuItem#getChildren()
	 */
	public List<IMenuItem> getChildren() {
		return m_children;
	}
	public void setChildren(final List<IMenuItem> children) {
		m_children = children;
	}
    /**
	 * @see to.etc.domui.components.menu.IMenuItem#getSearchString()
	 */
    public String getSearchString() {
        return byKey(m_searchKey);
    }
    /**
	 * @see to.etc.domui.components.menu.IMenuItem#getRequiredRights()
	 */
    public Right[] getRequiredRights() {
		return m_requiredRights;
	}
    /**
	 * @see to.etc.domui.components.menu.IMenuItem#getLabel()
	 */
    public String	getLabel() {
    	return byKey(m_labelKey);
    }
    /**
	 * @see to.etc.domui.components.menu.IMenuItem#getDescription()
	 */
    public String	getDescription() {
    	return byKey(m_descKey);
    }

    public boolean isSubMenu() {
    	return m_children.size() > 0;
    }
    public int getOrder() {
    	return m_order;
    }
    public void setOrder(final int order) {
		m_order = order;
	}
	public String getTitleKey() {
		return m_titleKey;
	}
	public void setTitleKey(final String titleKey) {
		m_titleKey = titleKey;
	}
}
