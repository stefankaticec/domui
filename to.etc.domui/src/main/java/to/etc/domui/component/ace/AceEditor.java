package to.etc.domui.component.ace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.parts.IComponentJsonProvider;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.javascript.JavascriptStmt;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * DomUI wrapper for the <a href="https://ace.c9.io/">ACE code editor</a>.
 *
 * Todo:
 * <pre>
 *     https://github.com/ajaxorg/ace/wiki/How-to-enable-Autocomplete-in-the-Ace-editor
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-12-17.
 */
public class AceEditor extends Div implements IControl<String>, IComponentJsonProvider {
	static private String m_version = "1.2.9";

	private int m_nextId;

	@Nullable
	private String m_value;

	@Nullable
	private String m_theme;

	@Nullable
	private String m_mode;

	private int m_gotoLine;

	private int m_tabSize = 4;

	private boolean m_disabled;

	private boolean m_readOnly;

	private boolean m_internalRo;

	private boolean m_mandatory;

	@Nullable
	private IValueChanged<?> m_valueChanged;

	@Nullable
	private Predicate<Character> m_prefixValidator;

	private boolean m_completerDefined;

	@FunctionalInterface
	public interface ICompletionHandler {
		@NonNull
		List<Completion> getCompletions(@NonNull String text, int row, int col, @NonNull String prefix) throws Exception;
	}

	@Nullable
	private ICompletionHandler m_completionHandler;

	@Override public void createContent() throws Exception {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdnjs.cloudflare.com/ajax/libs/ace/" + m_version + "/ace.js"), 10);

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("let ed = ace.edit('").append(getActualID()).append("');\n");
		sb.append("ed.__id='").append(getActualID()).append("';\n");
		sb.append("var Range = require('ace/range').Range;\n");
		sb.append("window['").append(getActualID()).append("'] = ed;\n");
		sb.append("WebUI.registerInputControl('").append(getActualID()).append("', {getInputField: function() {");
		sb.append(" return ed.getValue();\n");
		sb.append("},"
			+ "onVisibilityChanged: function() {"
			+ "  ed.resize();"
			//+ "  alert('rezi');"
			+ "}"
			+ "});\n");
		sb.append("ed.__markermap = {};\n");
		sb.append("ed.on(\"change\", ed.$onChangeBackMarker);\n");

		if(null != getOnValueChanged()) {
			sb.append("ed.getSession().on('change', function() {\n");
			sb.append(" if(ed.__utimer) {\n");
			sb.append("  clearTimeout(ed.__utimer);\n");
			sb.append("  delete ed.__utimer;\n");
			sb.append("}\n;");

			sb.append("  ed.__utimer = setTimeout(function() {\n");
			sb.append("    delete ed.__utimer;\n");
			sb.append("    WebUI.valuechanged('', '").append(getActualID()).append("');\n");

			sb.append("}, 500);\n");

			sb.append("});");
		}

		//-- Autocomplete?
		ICompletionHandler ch = getCompletionHandler();
		if(null != ch) {
			String js = FileTool.readResourceAsString(getClass(), "/resources/ace/acecompletion.js", "utf-8");
			sb.append(js.replace("$ID$", getActualID()));
			m_completerDefined = true;
		}
		sb.append("};\n");
		appendCreateJS(sb);
	}

	private void appendCompleterJS() {
		if(m_completerDefined)
			return;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("{ var ed = window['").append(getActualID()).append("'];");
			String js = FileTool.readResourceAsString(getClass(), "/resources/ace/acecompletion.js", "utf-8");
			sb.append(js.replace("$ID$", getActualID()));
			m_completerDefined = true;
			sb.append("}\n");
			appendJavascript(sb);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	@Override public Object provideJsonData(IPageParameters parameterSource) throws Exception {
		int col = parameterSource.getInt(getActualID() + "_col");
		int row = parameterSource.getInt(getActualID() + "_row");
		String prefix = parameterSource.getString(getActualID() + "_prefix");
		ICompletionHandler ch = getCompletionHandler();
		String value = m_value;
		if(ch == null || prefix == null || value == null) {
			return Collections.emptyList();
		}

		//-- Do we need to change the prefix?
		Predicate<Character> prefixValidator = m_prefixValidator;
		if(null != prefixValidator) {
			String dotted = getDottedPrefix(row, col, prefixValidator);
			if(null != dotted)
				prefix = dotted;

		}
		return ch.getCompletions(value, row, col, prefix);
	}

	@Override public void renderJavascriptState(StringBuilder sb) throws Exception {
		updateTheme();
		updateMode();
		updateValue();
		updateTabSize();
		updateReadOnly();

		super.renderJavascriptState(sb);
	}

	static public void initialize(UrlPage page) {
		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdnjs.cloudflare.com/ajax/libs/ace/" + m_version + "/ace.js"), 10);
		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdnjs.cloudflare.com/ajax/libs/ace/" + m_version + "/ext-language_tools.js"), 11);
	}

	@Override
	@Nullable public String getValue() {
		return m_value;
	}

	@Override
	public void setValue(@Nullable String value) {
		if(Objects.equals(value, m_value))
			return;
		markerClear();
		m_value = value;
		if(isBuilt()) {
			if(null == value)
				value = "";
			callStringMethod("setValue", value);
			callMethod("clearSelection");
		} else {
			forceRebuild();
		}
	}

	private void callStringMethod(String methodName, String value) {
		StringBuilder sb = new StringBuilder();
		handle(sb).append(methodName).append("(");
		if(null == value)
			sb.append("null");
		else
			StringTool.strToJavascriptString(sb, value, true);
		sb.append(");\n");
		appendJavascript(sb);
	}

	private void callSessionStringMethod(String methodName, String value) {
		StringBuilder sb = new StringBuilder();
		handle(sb).append("getSession().").append(methodName).append("(");
		if(null == value)
			sb.append("null");
		else
			StringTool.strToJavascriptString(sb, value, true);
		sb.append(");\n");
		appendJavascript(sb);
	}

	private void callMethod(String methodName, String... args) {
		StringBuilder sb = new StringBuilder();
		handle(sb).append(methodName).append("(");
		int count = 0;
		for(String arg : args) {
			if(count++ > 0)
				sb.append(",");
			sb.append(arg);
		}
		sb.append(");\n");
		appendJavascript(sb);
	}

	@Nullable public String getTheme() {
		return m_theme;
	}

	public void setTheme(@Nullable String theme) {
		if(Objects.equals(theme, m_theme))
			return;
		m_theme = theme;
		if(isBuilt()) {
			updateTheme();
		}
	}

	private void updateTheme() {
		String theme = m_theme;
		if(null == theme) {
			theme = "ace/theme/monokai";
		}
		callStringMethod("setTheme", theme);
	}

	private void updateMode() {
		String mode = m_mode;
		if(null == mode)
			mode = "ace/mode/javascript";
		callSessionStringMethod("setMode", mode);
	}

	private void updateValue() {
		String value = m_value;
		if(null == value)
			value = "";
		callStringMethod("setValue", value);
		callMethod("clearSelection");
	}

	private void updateTabSize() {
		if(m_tabSize > 0 && m_tabSize < 20)
			callMethod("getSession().setTabSize", Integer.toString(m_tabSize));
	}

	private void updateReadOnly() {
		callMethod("setReadOnly", Boolean.valueOf(m_internalRo).toString());
	}

	@Nullable public String getMode() {
		return m_mode;
	}

	public void setMode(@Nullable String mode) {
		if(Objects.equals(mode, m_mode))
			return;
		m_mode = mode;
		if(isBuilt())
			updateMode();
	}

	public int getTabSize() {
		return m_tabSize;
	}

	public void setTabSize(int tabSize) {
		if(m_tabSize == tabSize)
			return;
		m_tabSize = tabSize;
		if(! isBuilt())
			updateTabSize();
	}

	private StringBuilder handle(StringBuilder sb) {
		sb.append("window['").append(getActualID()).append("'].");
		return sb;
	}

	public void gotoLine(int line) {
		if(! isBuilt())
			m_gotoLine = line;
		else {
			callMethod("gotoLine", Integer.toString(line));
		}
	}

	public void gotoLine(int line, int col) {
		if(! isBuilt())
			m_gotoLine = line;
		else {
			callMethod("gotoLine", Integer.toString(line), Integer.toString(col));
		}
	}

	public void selectWord(int line, int col) {
		if(isBuilt()) {
			callMethod("selection.getWordRange", Integer.toString(line), Integer.toString(col));
		}
	}

	public void select(int line, int col, int line2, int col2) {
		if(isBuilt()) {
			line--;
			line2--;
			appendJavascript("var Range = require('ace/range').Range;\n");
			callMethod("selection.setRange", "new Range(" + line + "," + col + ", " + line2 + "," + col2 + ")", "true");
		}
	}

	public static String getVersion() {
		return m_version;
	}

	public static void setVersion(String version) {
		m_version = version;
	}

	public int getGotoLine() {
		return m_gotoLine;
	}

	public void setGotoLine(int gotoLine) {
		m_gotoLine = gotoLine;
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
		updateInternalRo(m_disabled || m_readOnly);
	}

	private void updateInternalRo(boolean ro) {
		if(m_internalRo == ro)
			return;

		m_internalRo = ro;
		if(isBuilt())
			updateReadOnly();
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		m_readOnly = readOnly;
		updateInternalRo(m_disabled || m_readOnly);
	}

	@Override public boolean isMandatory() {
		return m_mandatory;
	}

	@Override public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	@Override public IValueChanged<?> getOnValueChanged() {
		return m_valueChanged;
	}

	@Override public void setOnValueChanged(IValueChanged<?> onValueChanged) {
		m_valueChanged = onValueChanged;
	}

	/**
	 * The editor does not support being used as a label for= target.
	 */
	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	@Override public boolean acceptRequestParameter(@NonNull String[] values) throws Exception {
		if(values.length != 1)
			throw new IllegalStateException("? Expecting but one value?");
		String value = values[0];
		if(Objects.equals(m_value, value))
			return false;

		m_value = value;
		return true;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Code completion handling.									*/
	/*----------------------------------------------------------------------*/

	/**
	 * See {@link #setCompletionHandler(ICompletionHandler)}.
	 */
	public ICompletionHandler getCompletionHandler() {
		return m_completionHandler;
	}

	/**
	 * Set a completion handler used to handle Code Completion on CTRL+SPACE. The handler
	 * gets passed a prefix of the word typed so far, and the full data string. It should
	 * return a set of possible completions for that string that will then be presented to
	 * the user.
	 * The prefix, by default, is only scanned to contain identifier characters. This means
	 * that anything with a dot does not contain the dot nor the part before it. To control
	 * what characters can be valid as a prefix you need to use setPrefixPredicate(), or
	 * for basic identifiers you can call setPrefixAllowDotted().
	 */
	public void setCompletionHandler(ICompletionHandler completionHandler) {
		m_completionHandler = completionHandler;
		if(! m_completerDefined)
			appendCompleterJS();
	}

	/**
	 * Set a Predicate which defines whether a character of a prefix is valid for that
	 * prefix, when finding the part of code to complete.
	 */
	public void setPrefixPredicate(Predicate<Character> validchars) {
		m_prefixValidator = validchars;
	}

	public void setPrefixAllowDotted() {
		setPrefixPredicate(character -> character == '.' || Character.isJavaIdentifierPart(character));
	}

	/**
	 * Get a prefix which includes dots (for instance) for code completion.
	 */
	@Nullable
	public String getDottedPrefix(int row, int col, Predicate<Character> validchars) {
		String value = m_value;
		if(null == value)
			return null;
		int pos = StringTool.getPositionIn(value, row, col);		// position at cursor
		if(pos <= 1)
			return null;

		//-- Scan the part before
		int start = pos;
		while(start > 0) {
			char c = value.charAt(start - 1);
			if(! validchars.test(c)) {
				break;
			}
			start--;
		}
		return value.substring(start, pos);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Markers.													*/
	/*----------------------------------------------------------------------*/
	private Set<Marker> m_markerSet = new HashSet<>();

	private Set<Marker> m_oldMarkerSet = new HashSet<>();

	private Map<Integer, Marker> m_markerByIdMap = new HashMap<>();

	private Map<String, Marker> m_markerByIdentMap = new HashMap<>();

	@Nullable
	private PositionCalculator m_calculator;

	/**
	 * Delete all markers.
	 */
	public void markerClear() {
		m_markerSet.clear();
		m_markerByIdMap.clear();
		m_markerByIdentMap.clear();
		changedJavascriptState();
	}

	/**
	 * Adds the sepecified marker and returns the ID, which can be used to remove the marker again.
	 */
	public int markerAdd(MsgType sev, int line, int col, int endLine, int endCol, String message) {
		return markerAdd(sev, line, col, endLine, endCol, message, null);
	}

	/**
	 * Adds the sepecified marker and returns the ID, which can be used to remove the marker again.
	 */
	public int markerAdd(MsgType sev, int line, int col, int endLine, int endCol, String message, @Nullable String css) {
		if(line < 0)
			line = 0;
		if(endLine < 0)
			endLine = 0;
		if(col < 0)
			col = 0;
		if(endCol < 0)
			endCol = 0;
		if(line == endLine && endCol <= col)
			endCol = col + 5;

		//-- let's please Java's idiot architects: variables in their dumbass non-lambda's must be effectively final. Idiots.
		int uselesscol = col;
		int uselessEndLine = endLine;
		int uselessLine = line;
		int uselessEndCol = endCol;					// Do not repeat yourself my ass.

		String ident = sev.toString() + line + "/" + col + "/" + endLine + "/" + endCol + message;
		Marker marker = m_markerByIdentMap.computeIfAbsent(ident, a -> new Marker(m_nextId++, sev, message, uselessLine, uselesscol, uselessEndLine, uselessEndCol, css));
		m_markerSet.add(marker);
		m_markerByIdMap.put(marker.getId(), marker);
		changedJavascriptState();
		return marker.getId();
	}

	public int markerAdd(MsgType sev, int startPosition, int endPosition, String message, @Nullable String css) {
		String value = getValue();
		if(null == value || endPosition <= startPosition)
			return -1;
		PositionCalculator calculator = m_calculator;
		if(calculator == null || ! value.equals(calculator.getText())) {
			calculator = m_calculator = new PositionCalculator(value);
		}
		Point sp = new Point();
		Point ep = new Point();
		calculator.getXYPosition(sp, startPosition);
		calculator.getXYPosition(sp, endPosition);

		return markerAdd(sev, sp.y, sp.x, ep.y, ep.x, message, css);
	}

	public int markerAdd(MsgType sev, int startPosition, int endPosition, String message) {
		return markerAdd(sev, startPosition, endPosition, message, null);
	}

	/**
	 * Remove the marker by id (which was returned by {@link #markerAdd(MsgType, int, int, int, int, String)} before).
	 */
	public void markerRemove(int id) {
		Marker marker = m_markerByIdMap.get(id);
		if(null != marker) {
			m_markerSet.remove(marker);
			changedJavascriptState();
		}
	}

	/**
	 * Remove all markers for the specified location (and message).
	 */
	public void markerRemove(MsgType sev, int line, int col, int endLine, int endCol, String message) {
		String ident = sev.toString() + line + "/" + col + "/" + endLine + "/" + endCol + message;
		Marker marker = m_markerByIdentMap.remove(ident);
		if(null != marker) {
			m_markerSet.remove(marker);
			m_markerByIdMap.remove(marker.getId());
			changedJavascriptState();
		}
	}

	@Override protected void renderJavascriptDelta(JavascriptStmt b) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("let ed = ");
		sb.append("window['").append(getActualID()).append("'];\n");
		sb.append("let session = ed.getSession();\n");
		sb.append("let range_ = ace.require(\"ace/range\");\n");

		Set<Marker> set = new HashSet<>(m_oldMarkerSet);
		set.removeAll(m_markerSet);
		for(Marker marker : set) {
			sb.append("try {\n");
			sb.append("let mid=ed.__markermap['").append(marker.getId()).append("'];\n");
			sb.append("session.removeMarker(mid);\n");
			sb.append("delete ed.__markermap[" + marker.getId() + "];");
			//sb.append("console.log('del marker ").append(marker.getId()).append(" as ' + mid);\n");
			sb.append("} catch(x) { alert('Failed ' + x);\n");
			sb.append("}\n");
			//System.out.println(" delete marker " + marker);
		}
		//m_oldMarkerSet.removeAll(set);

		set = new HashSet<>(m_markerSet);
		set.removeAll(m_oldMarkerSet);						// Get all added thingies
		for(Marker marker : set) {
			sb.append("try {\n");
			sb.append("let range = new range_.Range(" + marker.getLine() + ", " + marker.getColumn() +"," + marker.getEndLine() + ", " + (marker.getEndColumn()) + ");\n");
			sb.append("range.start = session.doc.createAnchor(range.start);\n"
				+ "range.end = session.doc.createAnchor(range.end);\n"
			);
			String css = marker.getCssClass();
			if(null == css)
				css = "ui-ace-error";
			sb.append("let id = session.addMarker(range, '" + css + "');\n");
			sb.append("ed.__markermap['").append(marker.getId()).append("'] = id;\n");
			//sb.append("console.log('add marker " + marker.getId() + " as ' + id);\n");
			sb.append("} catch(x) {alert('error ' + x);}\n");
			//System.out.println(" add marker " + marker);
		}
		m_oldMarkerSet.clear();
		m_oldMarkerSet.addAll(m_markerSet);

		sb.append("ed.$onChangeBackMarker();\n");
		sb.append("}");
		b.append(sb.toString());
	}

	/**
	 * A completion result for the ICompletionHandler interface.
	 */
	public static final class Completion {
		private final String m_name;
		private final String m_value;
		private final String m_meta;
		private final int m_score;

		public Completion(String name, String value, String meta, int score) {
			m_name = name;
			m_value = value;
			m_meta = meta;
			m_score = score;
		}

		public String getName() {
			return m_name;
		}

		public String getValue() {
			return m_value;
		}

		public String getMeta() {
			return m_meta;
		}

		public int getScore() {
			return m_score;
		}
	}

	@NonNullByDefault
	private static final class Marker {
		private final int m_id;

		private final MsgType m_type;

		private final String m_message;

		private final int m_line;

		private final int m_column;

		private final int m_endLine;

		private final int m_endColumn;

		@Nullable
		private final String m_cssClass;

		public Marker(int id, MsgType type, String message, int line, int column, int endLine, int endColumn, @Nullable String cssClass) {
			m_id = id;
			m_type = type;
			m_message = message;
			m_line = line;
			m_column = column;
			m_endLine = endLine;
			m_endColumn = endColumn;
			m_cssClass = cssClass;
		}

		public int getId() {
			return m_id;
		}

		public MsgType getType() {
			return m_type;
		}

		public String getMessage() {
			return m_message;
		}

		public int getLine() {
			return m_line;
		}

		public int getColumn() {
			return m_column;
		}

		public int getEndLine() {
			return m_endLine;
		}

		public int getEndColumn() {
			return m_endColumn;
		}

		@Nullable
		public String getCssClass() {
			return m_cssClass;
		}

		@Override public boolean equals(@Nullable Object o) {
			if(this == o)
				return true;
			if(o == null || getClass() != o.getClass())
				return false;
			Marker marker = (Marker) o;
			return m_line == marker.m_line &&
				m_id == marker.m_id &&
				m_column == marker.m_column &&
				m_endLine == marker.m_endLine &&
				m_endColumn == marker.m_endColumn &&
				m_type == marker.m_type &&
				m_message.equals(marker.m_message) &&
				Objects.equals(m_cssClass, marker.m_cssClass);
		}

		@Override public int hashCode() {
			return Objects.hash(m_type, m_message, m_line, m_column, m_endColumn, m_endLine, m_cssClass, m_id);
		}

		@Override public String toString() {
			return "id=" + m_id + "(" + m_line + "," + m_column + ")";
		}
	}

}
