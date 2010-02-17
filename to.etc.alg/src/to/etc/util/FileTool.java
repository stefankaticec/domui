package to.etc.util;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;

import org.w3c.dom.*;

import to.etc.xml.*;

/**
 * Contains some often-used file subroutines.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class FileTool {
	private FileTool() {
	}

	/** The seed TS to use as base of names. */
	static private long	m_seed_ts;

	/** The sequence number. */
	static private long	m_index;

	static public synchronized File newDir(final File root) {
		for(;;) {
			String fn = makeName("td");
			File of = new File(root, fn);
			if(!of.exists()) {
				of.mkdirs();
				return of;
			}
		}
	}

	static public synchronized File makeTempFile(final File root) {
		for(;;) {
			String fn = makeName("tf");
			File of = new File(root, fn);
			if(!of.exists()) {
				try {
					of.createNewFile();
				} catch(Exception x) {
					if(x instanceof RuntimeException)
						throw (RuntimeException) x;
					throw new WrappedException(x);
				}
				return of;
			}
		}
	}

	static private String makeName(final String type) {
		StringBuffer sb = new StringBuffer(32);
		sb.append(type);
		add36(sb, m_seed_ts);
		sb.append('-');
		add36(sb, m_index++);
		return sb.toString();
	}

	static private void add36(final StringBuffer sb, long v) {
		if(v > 36)
			add36(sb, v / 36);
		v = v % 36;
		if(v < 10)
			sb.append((char) (v + '0'));
		else
			sb.append((char) (v - 10 + 'a'));
	}


	/**
	 *	Deletes all files in the directory. It skips errors and tries to delete
	 *  as much as possible. If elogb is not null then all errors are written
	 *  there.
	 */
	static public boolean dirEmpty(final File dirf) {
		return dirEmpty(dirf, null);
	}

	static public void deleteDir(final File f) {
		dirEmpty(f);
		f.delete();
	}

	/**
	 *	Deletes all files in the directory. It skips errors and tries to delete
	 *  as much as possible. If elogb is not null then all errors are written
	 *  there.
	 */
	@Deprecated
	static public boolean dirEmpty(final File dirf, final Vector<Object> elogb) {
		boolean hase = false;

		File[] ar = dirf.listFiles();
		if(ar == null)
			return true;

		for(int i = 0; i < ar.length; i++) {
			String name = ar[i].getName();
			if(!name.equals(".") && !name.equals("..")) {
				try {
					if(ar[i].isDirectory())
						dirEmpty(ar[i], elogb);
					if(!ar[i].delete())
						throw new IOException("Delete failed?");
				} catch(IOException x) {
					if(elogb != null) {
						elogb.add(dirf);
						elogb.add(x);
					}
					hase = true;

				}
			}
		}

		return !hase;
	}

	/**
	 * Returns the extension of a file. The extension DOES NOT INCLUDE the . If no
	 * extension is present then the empty string is returned ("").
	 */
	static public String getFileExtension(final String fn) {
		int s1 = fn.lastIndexOf('/');
		int s2 = fn.lastIndexOf('\\');
		if(s2 > s1)
			s1 = s2;
		if(s1 == -1)
			s1 = 0;

		int p = fn.lastIndexOf('.');
		if(p < s1)
			return "";
		return fn.substring(p + 1);
	}


	/**
	 *	Returns the start position of the filename extension in the string. If
	 *  the string has no extension then this returns -1.
	 */
	static public int findFilenameExtension(final String fn) {
		int slp = fn.lastIndexOf('/');
		int t = fn.lastIndexOf('\\');
		if(t > slp)
			slp = t; // Find last directory separator,

		//-- Now find last dot,
		int dp = fn.lastIndexOf('.');
		if(dp < t) // Before dir separator: dot is in directory part,
			return -1;
		return dp;
	}


	/**
	 *	Returns the file name excluding the suffix of the name. So test.java
	 *  returns test.
	 */
	static public String fileNameSansExtension(final String fn) {
		int slp = fn.lastIndexOf('/');
		int t = fn.lastIndexOf('\\');
		if(t > slp)
			slp = t; // Find last directory separator,

		//-- Now find last dot,
		int dp = fn.lastIndexOf('.');
		if(dp < t) // Before dir separator: dot is in directory part,
			return fn;
		return fn.substring(0, dp);
	}

	/**
	 * Copies a file from src to dest.
	 * @param destf
	 * @param srcf
	 * @throws IOException
	 * @deprecated Replaced by copyFile.
	 */
	@Deprecated
	static public void fileCopy(final File destf, final File srcf) throws IOException {
		copyFile(destf, srcf);
	}

	/**
	 * Copies a file.
	 *
	 * @param destf		the destination
	 * @param srcf		the source
	 * @throws IOException	the error
	 */
	static public void copyFile(final File destf, final File srcf) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(srcf);
			os = new FileOutputStream(destf);
			copyFile(os, is);
			destf.setLastModified(srcf.lastModified());
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Copies the inputstream to the outputstream.
	 * @param os
	 * @param is
	 * @throws IOException
	 * @deprecated Use copyFile instead.
	 */
	@Deprecated
	static public void fileCopy(final OutputStream os, final InputStream is) throws IOException {
		copyFile(os, is);
	}

	/**
	 * Copies the inputstream to the output stream.
	 *
	 * @param destf		the destination
	 * @param srcf		the source
	 * @throws IOException	the error
	 */
	static public void copyFile(final OutputStream os, final InputStream is) throws IOException {
		byte[] buf = new byte[8192];
		int sz;
		while(0 < (sz = is.read(buf)))
			os.write(buf, 0, sz);
	}

	static public void copyFile(final Writer w, final Reader r) throws IOException {
		char[] buf = new char[8192];
		int sz;
		while(0 < (sz = r.read(buf)))
			w.write(buf, 0, sz);
	}

	/**
	 * Copies an entire directory structure from src to dest. This copies the
	 * files from src into destd; it does not remove files in destd that are
	 * not in srcd. Use synchronizeDir() for that.
	 * @param destd
	 * @param srcd
	 * @throws IOException
	 */
	static public void copyDir(final File destd, final File srcd) throws IOException {
		if(!srcd.exists())
			return;
		if(srcd.isFile()) {
			copyFile(destd, srcd);
			return;
		}
		if(destd.exists() && destd.isFile())
			destd.delete();
		destd.mkdirs();

		//-- Right: on with the copy then
		File[] ar = srcd.listFiles();
		for(File sf : ar) {
			File df = new File(destd, sf.getName());
			if(sf.isFile()) // Source is a file?
			{
				if(df.isDirectory()) // But target is a directory?
					deleteDir(df); // Delete it,
				copyFile(df, sf); // Then copy the file.
			} else if(sf.isDirectory()) // Source is a directory
			{
				if(df.isFile()) // ... but target is a file now?
					df.delete(); // then delete it...
				copyDir(df, sf); // ..before copying
			}
		}
	}

	static public String readFileAsString(final File f) throws Exception {
		StringOutputBuffer sb = new StringOutputBuffer((int) f.length() + 20);
		readFileAsString(sb, f);
		return sb.getValue();
	}

	static public void readFileAsString(final iOutput o, final File f) throws Exception {
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		try {
			String line;
			while(null != (line = lr.readLine())) {
				o.output(line);
				o.output("\n");
			}
		} finally {
			lr.close();
		}
	}

	static public String readFileAsString(final File f, final String encoding) throws Exception {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			return readStreamAsString(is, encoding);
		} finally {
			if(is != null)
				is.close();
		}
	}

	/**
	 *  mbp, moved here from old DaemonBase with some adaptions. Reads the head
	 *  and tail lines of a text file into the stringbuffer.
	 *  The number of lines in the head is at most "headsize". If this
	 *  count is exceeded the read lines will be placed in a circular string buffer
	 *  of size "tailsize", and appended to the stringbuffer when the whole file
	 *  has been processed.
	 *  Note that if headsize plus tailsize exceeds the actual number of lines,
	 *  this means that the whole file will be placed in the stringbuffer.
	 *
	 *  Intended use is to mail (excerpts from) logfiles from Daemon processes.
	 */
	static public void readHeadAndTail(final StringBuffer sb, final File f, final int headsize, final int tailsize) throws Exception {
		LineNumberReader lr = null; // the reader
		String[] ring = null; // ring buffer for lines if more than N
		int ringix = 0; // index into ringbuffer
		int linecount = 0; // lines processed so far.

		try {
			lr = new LineNumberReader(new FileReader(f));
			String line;

			while(null != (line = lr.readLine())) {
				linecount++;
				if(ring != null) // processing tail ?
				{
					// Yes. Save this line into the ringbuffer
					if(ringix == tailsize)
						ringix = 0; // rollover back to zero if needed
					ring[ringix++] = line; // put line in ringbuffer
				} else {
					// Copy line to output stringbuffer
					sb.append(line); // Add the line,
					sb.append('\n'); // And a newline,
					if(linecount > headsize) // Reached requested nr of lines in head ?
						ring = new String[tailsize]; // Create the ringbuffer
				}
			}

			// Do we need to output the tail or did everything fit in the head ?
			if(ring != null) {
				// There is a tail
				if(linecount > (headsize + tailsize)) // And lines were skipped?
				{
					sb.append("\n ...\n ...\n ...");
					sb.append(linecount - headsize - tailsize);
					sb.append(" lines were skipped ...\n ...\n ...\n");
				}
				// index in ringbuffer was already advanced in read loop, but not checked for roll-over
				if(ringix == tailsize)
					ringix = 0;
				// if there is nothing at this point in the ringbuffer, start outputting
				// from zero up to here otherwise output from here on for tailsize counts

				if(ring[ringix] == null) {
					for(int ix = 0; ix < ringix - 1; ix++) {
						sb.append(ring[ix]);
						sb.append('\n');
					}
				} else {
					for(int ct = 0; ct < tailsize; ct++) {
						sb.append(ring[ringix++]);
						sb.append('\n');
						if(ringix == tailsize)
							ringix = 0;
					}
				}
			}
		} finally {
			try {
				if(lr != null)
					lr.close();
			} catch(Exception x) {}
		}
	}

	static public String readStreamAsString(final InputStream is, final String enc) throws Exception {
		StringOutputBuffer sb = new StringOutputBuffer(128);
		readStreamAsString(sb, is, enc);
		return sb.getValue();
	}

	static public void readStreamAsString(final iOutput o, final InputStream f, final String enc) throws Exception {
		Reader r = new InputStreamReader(f, enc);
		try {
			readStreamAsString(o, r);
		} finally {
			r.close();
		}
	}

	static public void readStreamAsString(final iOutput o, final Reader r) throws Exception {
		char[] buf = new char[4096];
		for(;;) {
			int ct = r.read(buf);
			if(ct < 0)
				break;
			o.output(new String(buf, 0, ct));
		}
	}

	static public String readStreamAsString(final Reader r) throws Exception {
		StringOutputBuffer sb = new StringOutputBuffer(128);
		readStreamAsString(sb, r);
		return sb.getValue();
	}

	static public void writeFileFromString(final File f, final String v, final String enc) throws Exception {
		OutputStream os = new FileOutputStream(f);
		try {
			writeFileFromString(os, v, enc);
		} finally {
			os.close();
		}
	}

	static public void writeFileFromString(final OutputStream os, final String v, final String enc) throws Exception {
		Writer w = new OutputStreamWriter(os, enc == null ? "UTF8" : enc);
		try {
			w.write(v);
		} finally {
			w.close();
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	File hash stuff..									*/
	/*--------------------------------------------------------------*/
	static public byte[] hashFile(final File f) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			return hashFile(is);
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	static public byte[] hashBuffers(final byte[][] data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}

		//-- Ok, calculate.
		for(byte[] b : data)
			md.update(b);
		return md.digest();
	}

	static public String hashBuffersHex(final byte[][] data) {
		return StringTool.toHex(hashBuffers(data));
	}

	/**
	 * Hashes all data from an input stream and returns an MD5 hash.
	 * @param is	The stream to read and hash.
	 * @return		A hash (16 bytes MD5)
	 * @throws IOException
	 */
	static public byte[] hashFile(final InputStream is) throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}

		//-- Ok, read and calculate.
		byte[] buf = new byte[8192];
		int szrd;
		while(0 <= (szrd = is.read(buf)))
			md.update(buf, 0, szrd);
		return md.digest();
	}

	static public String hashFileHex(final File f) throws IOException {
		return StringTool.toHex(hashFile(f));
	}

	static public String hashFileHex(final InputStream is) throws IOException {
		return StringTool.toHex(hashFile(is));
	}

	static public Properties loadProperties(final File f) throws Exception {
		InputStream is = new FileInputStream(f);
		try {
			Properties p = new Properties();
			p.load(is);
			return p;
		} finally {
			if(is != null)
				is.close();
		}
	}

	static public void saveProperties(final File f, final Properties p) throws Exception {
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			p.store(os, "# No comment");
		} finally {
			if(os != null)
				os.close();
		}
	}

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 * @param f
	 * @return
	 */
	static public Properties loadPropertiesFromZip(final File f, final String name) throws Exception {
		InputStream is = null;
		OutputStream os = null;

		//-- Try to locate a zipentry containing coma.jar
		try {
			is = new FileInputStream(f);
			return loadPropertiesFromZip(is, name);
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 * @param f
	 * @return
	 */
	static public Properties loadPropertiesFromZip(final InputStream is, final String name) throws Exception {
		ZipInputStream zis = null;
		OutputStream os = null;

		try {
			zis = new ZipInputStream(is);
			for(;;) {
				ZipEntry ze = zis.getNextEntry();
				if(ze == null)
					break;
				String n = ze.getName();
				if(n.equalsIgnoreCase(name)) {
					//-- Gotcha! Create parameters and load 'm
					Properties p = new Properties();
					p.load(zis); // Load properties
					return p;
				}
				zis.closeEntry();
			}
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(zis != null)
					zis.close();
			} catch(Exception x) {}
		}
		return null;
	}

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 * @param f
	 * @return
	 */
	static public Document loadXmlFromZip(final File f, final String name, final boolean nsaware) throws Exception {
		InputStream is = null;
		OutputStream os = null;

		//-- Try to locate a zipentry containing coma.jar
		try {
			is = new FileInputStream(f);
			return loadXmlFromZip(is, f + "!" + name, name, nsaware);
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 * @param f
	 * @return
	 */
	static public Document loadXmlFromZip(final InputStream is, final String ident, final String name, final boolean nsaware) throws Exception {
		ZipInputStream zis = null;
		OutputStream os = null;

		try {
			zis = new ZipInputStream(is);
			for(;;) {
				ZipEntry ze = zis.getNextEntry();
				if(ze == null)
					break;
				String n = ze.getName();
				if(n.equalsIgnoreCase(name)) {
					//-- Gotcha! Create parameters and load 'm
					return DomTools.getDocument(zis, ident, nsaware);
				}
				zis.closeEntry();
			}
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(zis != null)
					zis.close();
			} catch(Exception x) {}
		}
		return null;
	}

	/**
	 * Returns a stream which is the uncompressed data stream for a zip file
	 * component.
	 *
	* @param zipis
	* @return
	* @throws IOException
	*/
	static public InputStream getZipContent(final File zipfile, final String name) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(zipfile);
			InputStream isout = getZipContent(is, name);
			is = null;
			return isout;
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Returns a stream which is the uncompressed data stream for a zip file
	 * component.
	 *
	 * @param zipis
	 * @return
	 * @throws IOException
	 */
	static public InputStream getZipContent(final InputStream zipis, final String name) throws IOException {
		ZipInputStream zis = null;

		//-- Try to locate a zipentry for the spec'd name
		try {
			zis = new ZipInputStream(zipis);
			for(;;) {
				ZipEntry ze = zis.getNextEntry();
				if(ze == null)
					break;
				String n = ze.getName();
				if(n.equalsIgnoreCase(name)) {
					final ZipInputStream z = zis;
					zis = null;
					return new InputStream() {
						@Override
						public long skip(final long size) throws IOException {
							return z.skip(size);
						}

						@Override
						public synchronized void reset() throws IOException {
							z.reset();
						}

						@Override
						public int read(final byte[] b) throws IOException {
							return z.read(b);
						}

						@Override
						public int read(final byte[] b, final int off, final int len) throws IOException {
							return z.read(b, off, len);
						}

						@Override
						public int read() throws IOException {
							return z.read();
						}

						@Override
						public void close() throws IOException {
							z.close();
							zipis.close();
							super.close();
						}

						@Override
						public int available() throws IOException {
							return z.available();
						}
					};
				}
				zis.closeEntry();
			}
			return null;
		} finally {
			try {
				if(zis != null)
					zis.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Creates a classloader to load data from the given jar file.
	 * @param f
	 * @return
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	static public ClassLoader makeJarLoader(final File f) throws MalformedURLException {
		URL u = f.toURL();
		URLClassLoader uc = URLClassLoader.newInstance(new URL[]{u});
		return uc;
	}

	static public ClassLoader makeJarLoader(final File f, final ClassLoader parent) throws MalformedURLException {
		URL u = f.toURL();
		URLClassLoader uc = URLClassLoader.newInstance(new URL[]{u}, parent);
		return uc;
	}

	static public void copyResource(final Writer w, final Class< ? > cl, final String rid) throws Exception {
		Reader r = null;
		try {
			InputStream is = cl.getResourceAsStream(rid);
			if(is == null)
				throw new IllegalStateException("Missing resource '" + rid + "' at class=" + cl.getName());
			r = new InputStreamReader(is, "utf-8");
			copyFile(w, r);
		} finally {
			if(r != null)
				r.close();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Zip and unzip.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Zip the contents of dir or file to the zipfile. The zipfile
	 * is deleted before the new contents are added to it.
	 */
	public static void zip(final File zipfile, final File dir) throws Exception {
		if(zipfile.exists())
			if(!zipfile.delete())
				throw new IOException("Unable to delete zipfile: " + zipfile);

		ZipOutputStream zos = null;
		InputStream is = null;
		byte[] buf = new byte[8192]; // Copy buffer
		try {
			//-- Create the output stream
			zos = new ZipOutputStream(new FileOutputStream(zipfile));
			if(dir.isFile())
				zipFile(zos, "", dir, buf);
			else
				zipDir(zos, "", dir, buf); // Zip the entry passed
		} finally {
			if(zos != null)
				try {
					zos.close();
				} catch(Exception x) {}
			if(is != null)
				try {
					is.close();
				} catch(Exception x) {}
		}
	}

	static private void zipDir(final ZipOutputStream zos, final String base, File f, final byte[] buf) throws IOException {
		if(!f.isDirectory())
			throw new IllegalStateException("Must be a directory");

		//-- Write a directory entry if there's something
		if(base.length() > 0) {
			ZipEntry ze = new ZipEntry(base); // The dir ending in /
			zos.putNextEntry(ze);
			ze.setTime(f.lastModified());
		}

		//-- Now recursively copy the rest.
		File[] far = f.listFiles(); // All files in the dir
		for(int i = 0; i < far.length; i++) {
			f = far[i];
			if(f.isFile())
				zipFile(zos, base, f, buf); // just zip the file,
			else
				zipDir(zos, base + f.getName() + "/", f, buf);
		}
	}

	/**
	 * Recursive workhorse for zipping the entry passed, be it file or directory.
	 * @param zos
	 * @param base
	 * @param f
	 * @throws IOException
	 */
	static private void zipFile(final ZipOutputStream zos, final String base, final File f, final byte[] buf) throws IOException {
		//-- Create a relative name for this entry
		if(!f.isFile())
			throw new IllegalStateException(f + ": must be file");
		InputStream is = null;
		try {
			//-- Write this file.
			ZipEntry ze = new ZipEntry(base + f.getName());
			ze.setTime(f.lastModified());
			zos.putNextEntry(ze);

			//-- Copy
			is = new FileInputStream(f);
			int sz;
			while(0 <= (sz = is.read(buf)))
				zos.write(buf, 0, sz);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch(Exception x) {}
		}
	}

	/**
	 * Unzip the contents of the zipfile to the directory. The directory is
	 * created if it does not yet exist.
	 */
	public static void unzip(final File dest, final File zipfile) throws Exception {
		dest.mkdirs();

		ZipInputStream zis = null;
		OutputStream os = null;
		byte[] buf = new byte[8192];
		try {
			if(zipfile.length() < 1)
				return;

			zis = new ZipInputStream(new FileInputStream(zipfile));
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) {
				File of = new File(dest, ze.getName()); // Create a full path
				if(ze.isDirectory())
					of.mkdirs();
				else {
					//-- Copy.
					File dir = of.getParentFile();
					dir.mkdirs();
					os = new FileOutputStream(of);
					int sz;
					while(0 < (sz = zis.read(buf)))
						os.write(buf, 0, sz);
					os.close();
					os = null;
				}
				zis.closeEntry();
			}
		} finally {
			if(zis != null)
				try {
					zis.close();
				} catch(Exception x) {}
			if(os != null)
				try {
					os.close();
				} catch(Exception x) {}
		}
	}

	/**
	 * prepare a directory in this way: if it does not exist, create it.
	 * if it does exist then delete all files from the dir.
	 * @param dir the directory that must be made existent
	 * @throws Exception when creation fails or when removing old contents
	 * fails.
	 */

	public static void prepareDir(final File dir) throws Exception {
		if(!dir.exists()) {
			if(dir.mkdirs()) // make sure all parent dirs exist, then create this one
				return; // success

			throw new Exception("unable to create directory: " + dir.getPath());
		} else {
			if(!dirEmpty(dir))
				throw new Exception("unable to empty the directory: " + dir.getPath());
		}
	}

	/**
	 * Loads a byte[][] from an input stream until exhaustion.
	 * @param is
	 * @return
	 */
	static public byte[][] loadByteBuffers(final InputStream is) throws IOException {
		ArrayList<byte[]> al = new ArrayList<byte[]>();
		byte[] buf = new byte[8192];
		int off = 0;
		for(;;) {
			//-- Fill the (next part of the) buffer
			int max = buf.length - off;
			int sz = is.read(buf, off, max);
			if(sz == -1) {
				//-- EOF - data complete.
				if(off <= 0)
					break;
				byte[] newbuf = new byte[off];
				System.arraycopy(buf, 0, newbuf, 0, off);
				al.add(newbuf);
				break;
			}
			off += sz;
			if(off >= buf.length) {
				al.add(buf);
				off = 0;
				buf = new byte[8192];
			}
		}
		return al.toArray(new byte[al.size()][]);
	}

	static public byte[][] loadByteBuffers(final File in) throws IOException {
		InputStream is = new FileInputStream(in);
		try {
			return loadByteBuffers(is);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	static public void save(final File of, final byte[][] data) throws IOException {
		OutputStream os = new FileOutputStream(of);
		try {
			save(os, data);
		} finally {
			try {
				os.close();
			} catch(Exception x) {}
		}
	}

	static public void save(final OutputStream os, final byte[][] data) throws IOException {
		for(byte[] b : data)
			os.write(b);
	}

	/**
	 * Returns the java.io.tmpdir directory. Throws an exception if it does not exist or
	 * is inaccessible.
	 *
	 * @return
	 */
	static public File getTmpDir() {
		String v = System.getenv("java.io.tmpdir");
		if(v == null)
			v = "/tmp";
		File tmp = new File(v);
		if(!tmp.exists() || !tmp.isDirectory())
			throw new IllegalStateException("The 'java.io.tmpdir' variable does not point to an existing directory (" + tmp + ")");
		return tmp;
	}
	static {
		m_seed_ts = System.currentTimeMillis();
	}


	/**
	 * Sends an int fragment
	 * @param val
	 * @throws Exception
	 */
	static public void writeInt(final OutputStream os, final int val) throws IOException {
		os.write((val >> 24) & 0xff);
		os.write((val >> 16) & 0xff);
		os.write((val >> 8) & 0xff);
		os.write(val & 0xff);
	}

	/**
	 * Sends a long
	 * @param val
	 * @throws Exception
	 */
	static public void writeLong(final OutputStream os, final long val) throws IOException {
		writeInt(os, (int) (val >> 32));
		writeInt(os, (int) (val & 0xffffffff));
	}

	static public void writeString(final OutputStream os, String s) throws IOException {
		if(s == null)
			s = "";
		byte[] data = s.getBytes("UTF-8");
		//        msg("hex of '"+s+"' is "+StringTool.toHex(data)+" ("+data.length+" bytes)");
		writeInt(os, data.length);
		os.write(data);
	}

	/**
	 * Reads a 4-byte bigendian int off the connection.
	 * @return
	 * @throws Exception
	 */
	static public int readInt(final InputStream is) throws IOException {
		int v1 = is.read();
		int v2 = is.read();
		int v3 = is.read();
		int v4 = is.read();
		return (v1 << 24) | (v2 << 16) | (v3 << 8) | v4;
	}

	static public long readLong(final InputStream is) throws IOException {
		int v1 = readInt(is);
		int v2 = readInt(is);
		return (long) v1 << 32 | ((long) v2 & 0xffffffff);
	}

	static public String readString(final InputStream is) throws IOException {
		int sl = readInt(is);
		if(sl < 0)
			return null;
		byte[] data = new byte[sl];
		if(is.read(data) != sl)
			throw new IOException("String could not be fully read");
		return new String(data, "UTF-8");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Logging helpers.									*/
	/*--------------------------------------------------------------*/

	static public InputStream wrapInputStream(final InputStream rawStream, final LogSink s, final int maxinmemory) throws Exception {
		//-- Read the input stream and copy to memory or file.
		File tempfile = null;
		byte[] buf = new byte[maxinmemory];
		InputStream is = rawStream;
		int off = 0;

		//-- Initial read of a bufferfull. Exit if the buffer is full AND more data is available(!)
		for(;;) {
			int szleft = buf.length - off; // #bytes left in buffert
			if(szleft == 0) // Buffer overflow: need big file.
				break;

			int szread = is.read(buf, off, szleft); // Read fully,
			if(szread == -1) {
				//-- Got EOF within single buffer: no need to read further, use memory stream. Off now contains the length read totally. Start to dump the data,
				StringBuilder sb = new StringBuilder(off * 4);
				sb.append("Raw INPUT dump of the input stream:\n");
				for(int doff = 0; doff < off; doff += 32) {
					StringTool.arrayToDumpLine(sb, buf, doff, 32);
					sb.append("\n");
				}
				sb.append("Total size of the input stream is " + off + " bytes\n");
				s.log(sb.toString());
				return new ByteArrayInputStream(buf, 0, off); // Return the memory based copy.
			}

			//-- Read completed, but room to spare: try again,
			off += szread;
		}

		//-- Ok: the buffer overflowed. We allocate a tempfile and dump the data in there.
		OutputStream os = null;
		try {
			tempfile = File.createTempFile("soapin", ".bin");
			os = new FileOutputStream(tempfile);
			os.write(buf); // Copy what's already read.

			//-- Dump what's already read into a string thing
			StringBuilder sb = new StringBuilder(off * 4);
			sb.append("Raw INPUT dump of the input stream:\n");
			int doff = 0;
			for(; doff < buf.length; doff += 32) {
				StringTool.arrayToDumpLine(sb, buf, doff, 32);
				sb.append("\n");
			}

			//-- Now continue reading buffers, dumping 'm and adding them to the file.
			for(;;) {
				int szread = is.read(buf);
				if(szread <= 0)
					break;

				//-- Push data read to the overflow file
				os.write(buf, 0, szread);

				//-- Log whatever's read,
				for(int rlen = 0; rlen < szread; rlen += 32) {
					StringTool.arrayToDumpLine(sb, buf, rlen, 32);
					sb.append("\n");
					doff += 32;
				}
				off += szread;
			}
			os.close();
			os = null;

			//-- Log the data,
			sb.append("Total size of the input stream is " + off + " bytes\n");
			s.log(sb.toString());
			final InputStream tis = new FileInputStream(tempfile);
			final File del = tempfile;
			tempfile = null;

			return new InputStream() {
				@Override
				public int read() throws IOException {
					return tis.read();
				}

				@Override
				public int read(final byte[] b, final int xoff, final int len) throws IOException {
					return tis.read(b, xoff, len);
				}

				@Override
				public void close() throws IOException {
					tis.close();
					del.delete();
				}
			};
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(tempfile != null)
					tempfile.delete();
			} catch(Exception x) {}
		}
	}

	static public InputStream copyAndDumpStream(StringBuilder tgt, InputStream in, String encoding) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
		copyFile(bos, in);
		bos.close();

		byte[] data = bos.toByteArray(); // Data read from stream;
		tgt.append(new String(data, encoding));
		return new ByteArrayInputStream(data);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Miscellaneous.										*/
	/*--------------------------------------------------------------*/
	/**
	 * This attempts to close all of the resources passed to it, without throwing exceptions. It
	 * is meant to be used from finally clauses. Please take care: objects that require a succesful
	 * close (like writers or outputstreams) should NOT be closed by this method! They must be
	 * closed using a normal close within the exception handler.
	 * This list can also contain File objects; these files/directories will be deleted.
	 * @param list
	 */
	static public void closeAll(Object... list) {
		//-- Level 0 closes
		int tox = 0;
		for(Object v : list) {
			try {
				if(v instanceof Closeable) {
					((Closeable) v).close();
				} else if(v instanceof ResultSet) {
					((ResultSet) v).close();
				} else if(v instanceof File) {
					File f = (File) v;
					if(f.isFile())
						f.delete();
					else
						FileTool.deleteDir(f);
				} else if(v != null)
					list[tox++] = v; // Keep, todo
			} catch(Exception x) {
				Logger.getLogger(FileTool.class.getName()).log(Level.FINE, "Cannot close resource " + v + " (a " + v.getClass() + "): " + x, x);
			}
		}

		//-- Next level: statements..
		int len = tox;
		tox = 0;
		for(int i = 0; i < len; i++) {
			Object v = list[i];
			try {
				if(v instanceof Statement) {
					((Statement) v).close();
				} else
					list[tox++] = v; // Keep, todo
			} catch(Exception x) {
				Logger.getLogger(FileTool.class.getName()).log(Level.FINE, "Cannot close resource " + v + " (a " + v.getClass() + "): " + x, x);
			}
		}

		//-- Last level: everything else.
		len = tox;
		tox = 0;
		for(int i = 0; i < len; i++) {
			Object v = list[i];
			try {
				if(v instanceof Connection) {
					((Connection) v).close();
					v = null;
				} else {
					Method m = ClassUtil.findMethod(v.getClass(), "close", null);
					if(m == null) {
						m = ClassUtil.findMethod(v.getClass(), "release", null);
					}
					if(m != null) {
						m.invoke(v);
						v = null;
					}
				}
			} catch(Exception x) {
				Logger.getLogger(FileTool.class.getName()).log(Level.FINE, "Cannot close resource " + v + " (a " + v.getClass() + "): " + x, x);
			}

			if(v != null) {
				StringTool.dumpLocation("UNKNOWN RESOURCE OF TYPE " + v.getClass() + " TO CLOSE PASSED TO FileTool.closeAll()!!!!\nFIX THIS IMMEDIATELY!!!!!!");
			}
		}
	}
}
