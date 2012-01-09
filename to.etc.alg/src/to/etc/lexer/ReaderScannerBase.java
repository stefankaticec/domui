package to.etc.lexer;

import java.io.*;

public class ReaderScannerBase extends TextReaderBase {
	static public final int		T_EOF				= -1;

	static public final int		T_STRING			= -2;

	static public final int		T_NUMBER			= -3;

	static public final int		T_IPADDR			= -4;

	static public final int		T_IDENT				= -5;

	static public final int		T_COMMENT			= -6;

	static public final int		T_BASE_LAST			= -7;

	private int					m_token_lnr;

	private int					m_token_cnr;

	private boolean				m_allowNewlineInString;

	public ReaderScannerBase(Object source, Reader r) {
		super(source, r);
	}

	public boolean isAllowNewlineInString() {
		return m_allowNewlineInString;
	}

	public void setAllowNewlineInString(boolean allowNewlineInString) {
		m_allowNewlineInString = allowNewlineInString;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Current token data.									*/
	/*--------------------------------------------------------------*/
	//	public String getText() {
	//		return getCopied();
	//	}

	public int getTokenLine() {
		return m_token_lnr;
	}

	public int getTokenColumn() {
		return m_token_cnr;
	}

	public SourceLocation getSourceLocation() {
		return new SourceLocation(this);
	}

	public void error(String msg) throws SourceErrorException {
		throw new SourceErrorException(getSourceLocation(), msg);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Lexical Construct handlers.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Skips whitespace until current character is either EOF or non-ws.
	 * @throws IOException
	 */
	protected void skipWs() throws IOException {
		for(;;) {
			int c = LA();
			if(c == -1 || !Character.isWhitespace((char) c))
				return;
			accept();
		}
	}

	/**
	 * Skips whitespace until current character is either EOF or non-ws.
	 * @throws IOException
	 */
	protected void skipWsNoNL() throws IOException {
		for(;;) {
			int c = LA();
			if(c == -1 || c == '\n' || !Character.isWhitespace((char) c))
				return;
			accept();
		}
	}

	/**
	 * Scans a very simple string: something starting with something, terminating
	 * with the same something and not allowing anything ugly in between.
	 * @throws IOException
	 */
	protected void scanSimpleString(boolean keepquotes) throws IOException, SourceErrorException {
		int qc = LA(); // Get quote start
		accept();
		if(!keepquotes)
			clearCopy();
		else
			append((char) qc);
		for(;;) {
			int c = LA();
			if(c == qc)
				break;
			else if(c == -1) {
				error("Unexpected EOF in string constant started at line " + m_token_lnr + ":" + m_token_cnr);
				//				throw new IllegalStateException("Unexpected EOF in string constant started at line " + m_token_lnr + ":" + m_token_cnr);
			} else if(c == '\n' && !isAllowNewlineInString())
				error("Unexpected newline in string constant started at line " + m_token_lnr + ":" + m_token_cnr + " (collected was " + getCopied() + ")");
			//				throw new IllegalStateException("Unexpected newline in string constant started at line " + m_token_lnr + ":" + m_token_cnr + " (collected was " + m_sb.toString() + ")");
			append((char) c);
			accept();
		}
		if(keepquotes)
			append(qc);
		accept();
	}

	protected int scanNumber() throws IOException {
		int c = LA(); // Get current numeral
		append(c); // Add 1st digit
		accept();
		int c2 = LA();
		int base = 10;
		if(c == '0') {
			if(c2 == 'x' || c2 == 'X') // Hex #?
			{
				base = 16;
				append(c2);
				accept();
			} else
				base = 8;
		}

		int ndots = 0;
		for(;;) {
			c = LA();
			if(c == -1)
				break;
			if(c == '.') {
				if(LA(1) == '.') {
					return T_NUMBER;
				} else {
					ndots++;
					accept();
					append(c);
				}
			} else {
				if(c >= '0' && c <= '9') {
					copy();
				} else {
					if(base <= 10)
						break;
				}

				if(base > 10) {
					if((c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))
						copy();
					else
						break;
				}
			}
		}
		if(ndots <= 1)
			return T_NUMBER;
		else if(ndots == 3)
			return T_IPADDR;
		else
			throw new IllegalStateException("Odd number or IP address started at line " + m_token_lnr + ":" + m_token_cnr);
	}

	protected int scanIdentifier() throws IOException {
		for(;;) {
			int c = LA();
			if(!isIdChar((char) c))
				return T_IDENT;
			accept();
			append(c);
		}
	}

	protected boolean isIdStart(char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}

	protected boolean isIdChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '$';
	}

	/**
	 * Called when a new token retrieve is started. This saves the current
	 * position within the file as the start location for the token and
	 * resets the token collection buffer.
	 */
	public void startToken() {
		m_token_cnr = getCurrentColumn();
		m_token_lnr = getCurrentLine();
		clearCopy();
	}
}
