/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.smtp;

import java.io.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Write MIME messages. Allows embedding MIME bodies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 24, 2010
 */
public class MimeWriter {
	static private final byte[]	CRLF				= {13, 10};

	static private final byte[]	DASHDASH			= {'-', '-'};

	@Nullable
	final private MimeWriter	m_dad;

	@Nonnull
	final private OutputStream	m_os;

	private String			m_currentEncoding	= "UTF-8";

	/** The unique boundary for this mime body. Does not contain ANY of the required -- and crlf crud. */
	private byte[]			m_boundary;

	private String				m_boundaryString;

	private MimeWriter			m_currentSub;

	protected MimeWriter(OutputStream os) {
		m_os = os;
		m_dad = null;
	}

	protected MimeWriter(MimeWriter dad, OutputStream os) {
		m_os = os;
		m_dad = dad;
	}

	protected byte[] getBoundary() {
		if(m_boundary == null) {
			m_boundaryString = "----bou-n-dar-y-=_" + StringTool.generateGUID();
			try {
				m_boundary = m_boundaryString.getBytes("iso-8859-1");
			} catch(Exception x) {}
		}
		return m_boundary;
	}

	protected String getBoundaryString() {
		if(m_boundary == null)
			getBoundary();
		return m_boundaryString;
	}

	/**
	 * Write the specified data TO this writer's output.
	 * @param data
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	public void write(byte[] data, int off, int len) throws IOException {
		m_os.write(data, off, len);
	}

	final public void write(byte[] data) throws IOException {
		write(data, 0, data.length);
	}

	/**
	 * Writes a string in the current encoding.
	 * @param s
	 */
	public void write(String s) throws IOException {
		byte[] data = s.getBytes(m_currentEncoding);
		write(data);
	}

	protected void rawHeader(String name, String value) throws IOException {
		write(name);
		write(": ");
		write(value);
		write("\r\n");
	}

	/**
	 * Write a boundary segment for a 'next' part.
	 */
	protected void writeOpenBoundary() throws IOException {
		write(CRLF); // Start of boundary on new line, ends last block. This is actually part of the boundary.
		write(DASHDASH);
		write(getBoundary());
		write(CRLF);
	}

	protected void writeCRLF() throws IOException {
		write(CRLF);
	}

	private void flush() throws IOException {
		if(m_currentSub != null) {
			m_currentSub.close();
			m_currentSub = null;
		}
		if(m_part_os != null) {
			m_part_os.close();
			m_part_os = null;
		}
	}

	/**
	 * Write the final boundary terminating the mime body.
	 * @throws IOException
	 */
	public void close() throws IOException {
		flush();
		write(CRLF); // Start of boundary on new line, ends last block. This is actually part of the boundary.
		write(DASHDASH);
		write(getBoundary());
		write(DASHDASH);
		write(CRLF);
		if(m_dad != null) {
			m_dad.m_currentSub = null;
		}
	}

	public void writeVersion() throws IOException {
		rawHeader("Mime-Version", "1.0");
	}

	public void writeBody(String contenttype, String trailer) throws IOException {
		rawHeader("Content-Type", contenttype + "; boundary=\"" + getBoundaryString() + "\";" + trailer);
	}

	static public MimeWriter createMimeWriter(OutputStream os, String contentType, String rest) throws IOException {
		if(!contentType.startsWith("multipart/"))
			throw new IllegalStateException("Expecting a 'multipart/' type");
		MimeWriter w = new MimeWriter(os);
		w.writeVersion();
		w.writeBody(contentType, rest);
		return w;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Part management.									*/
	/*--------------------------------------------------------------*/
	/** The outputstream which writes a part's data. */
	private OutputStream	m_part_os;

	private Writer			m_part_w;

	private boolean			m_inpart;

	private boolean			m_part_base64;

	public void partStart(boolean base64, String contenttype, String rest) throws IOException {
		flush(); // Close anything partially open

		writeOpenBoundary();
		writeBody(contenttype, rest); // Root document
		rawHeader("Content-Transfer-Encoding", base64 ? "base64" : "8bit");
		m_part_base64 = base64;
		m_inpart = true;
	}

	/**
	 * Checks to make sure we're writing a part currently.
	 */
	private void checkInPart() {
		if(!m_inpart)
			throw new IllegalStateException("Only valid when writing a mime part - call partStart() first");
	}

	/**
	 * Checks to make sure we're still allowed to write part headers.
	 */
	private void checkInPartHeaders() {
		checkInPart();
		if(m_part_os != null)
			throw new IllegalStateException("Headers can only be written before the MIME part's output stream is allocated");
	}

	public void partHeader(String name, String value) throws IOException {
		checkInPartHeaders();
		rawHeader(name, value);
	}

	/**
	 * Returns the content stream for the current part.
	 * @return
	 */
	public OutputStream partStream() throws IOException {
		checkInPart();
		if(m_currentSub != null)
			throw new IllegalStateException("I am writing an embedded mime body - close it before continuing");

		if(m_part_os == null) {
			//-- Terminate the part's headers with an empty line.
			writeCRLF();

			if(m_part_base64)
				m_part_os = new Base64OutputStream(m_os, false);
			else
				m_part_os = m_os; //new QuotedPrintableOutputStream(m_os);
		}
		return m_part_os;
	}

	/**
	 * Get a writer which allows writing content to the data in the appropriate format and encoding.
	 * @return
	 */
	public Writer partWriter(String encoding) throws IOException {
		if(m_part_w == null) {
			OutputStream os = partStream();
			m_part_w = new OutputStreamWriter(os, encoding);
		}
		return m_part_w;
	}

	/**
	 * Create a MIME part in THIS version that is a full MIME compound document itself. This creates the
	 * boundary header and the initial MIME headers.
	 * @return
	 */
	public MimeWriter createSubMime(String contenttype, String rest) throws IOException {
		flush(); // Close all other open subtypes
		writeOpenBoundary();

		MimeWriter sub = new MimeWriter(this, m_os);
		sub.writeVersion(); // Mime header
		sub.writeBody(contenttype, rest); // Root document
		m_currentSub = sub;
		return sub;
	}

}