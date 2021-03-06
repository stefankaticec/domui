package to.etc.security;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.LineIterator;
import to.etc.util.StringTool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-2-19.
 */
public class SshKeyUtils {
	public static final String SSH_RSA = "ssh-rsa";

	/*----------------------------------------------------------------------*/
	/*	CODING:	SSH format keys												*/
	/*----------------------------------------------------------------------*/
	/*
	 * https://www.cryptosys.net/pki/rsakeyformats.html
	 */

	/**
	 * Decodes a id_xxx.pub format key, like:
	 * <pre>
	 *     ssh-rsa AAAAB3N....== jal@etc.to
	 * </pre>
	 *
	 * See https://stackoverflow.com/questions/3706177/how-to-generate-ssh-compatible-id-rsa-pub-from-java
	 */
	static public PublicKey decodeSshPublicKey(String text) throws KeyFormatException {
		try {
			// Remove any newlines that can be the result of pasting.
			text = text.replace("\r", "").replace("\n", "");
			String[] split = text.split("\\s+");
			if(split.length < 2)
				throw new KeyFormatException("ssh key format not recognised");
			if(SSH_RSA.equals(split[0])) {
				byte[] data = StringTool.decodeBase64(split[1]);
				try(DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
					byte[] buf = readIntLenBytes(dis);			// ssh-rsa signature
					if(buf.length != 7 || !Arrays.equals(buf, SSH_RSA.getBytes()))
						throw new KeyFormatException("Expecting byte pattern ssh-rsa");
					BigInteger exp = new BigInteger(readIntLenBytes(dis));
					BigInteger mod = new BigInteger(readIntLenBytes(dis));
					return new RSAPublicKeyImpl(exp, mod, "RSA", "RFC4251", data);
				}
			}
		} catch(Exception x) {
			throw new KeyFormatException(x, "Failed to decode public key");
		}
		throw new KeyFormatException("Key format not recognised");
	}

	private static byte[] readIntLenBytes(DataInputStream dis) throws Exception {
		int l = dis.readInt();						// length of public exponent
		if(l < 0 || l > 8192)
			throw new KeyFormatException("Bad length");
		byte[] buf = new byte[l];
		if(dis.read(buf) != l)
			throw new KeyFormatException("Bad length");
		return buf;
	}

	static public String encodeSshPublicKey(PublicKey key, @Nullable String userId) throws KeyFormatException {
		String algo = key.getAlgorithm();
		if("RSA".equalsIgnoreCase(algo)) {
			try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				try(DataOutputStream dos = new DataOutputStream(bos)) {
					byte[] bytes = SSH_RSA.getBytes();
					dos.writeInt(bytes.length);
					dos.write(bytes);

					RSAPublicKey p = (RSAPublicKey) key;
					BigInteger exp = p.getPublicExponent();
					bytes = exp.toByteArray();
					dos.writeInt(bytes.length);
					dos.write(bytes);

					BigInteger mod = p.getModulus();
					bytes = mod.toByteArray();
					dos.writeInt(bytes.length);
					dos.write(bytes);

					if(userId == null)
						userId = "unknown";
					return "ssh-rsa"
						+ " " + StringTool.encodeBase64ToString(bos.toByteArray())
						+ " " + userId;
				}
			} catch(IOException x) {
				throw new KeyFormatException(x, "Nonsense");
			}
		}

		throw new KeyFormatException("Unsupported key algorithm: " + algo);
	}

	/**
	 *
	 */
	static public PrivateKey decodeSshPrivateKey(String key) throws KeyFormatException {
		try {
			if(key.startsWith("-----BEGIN RSA PRIVATE KEY-----")) {		// PKCS#1
				//-- PEM like format. Strip pem lines
				byte[] pkcs1 = readPemFormat(key);
				byte[] pkcs8 = decodeRsaPKCS1PrivateKey(pkcs1);

				EncodedKeySpec pks = new PKCS8EncodedKeySpec(pkcs8);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				return kf.generatePrivate(pks);
			} else if(key.startsWith("-----BEGIN ENCRYPTED PRIVATE KEY-----")) {

			}
		} catch(Exception x) {
			throw new KeyFormatException(x, "Bad or unknown format");
		}
		throw new KeyFormatException("Unknown format");
	}

	/**
	 * Change the unencrypted PKCS#1 RSA private key format to PKCS#8.
	 *
	 * See
	 * https://stackoverflow.com/questions/45646808/convert-an-rsa-pkcs1-private-key-string-to-a-java-privatekey-object
	 * https://stackoverflow.com/questions/23709898/java-convert-dkim-private-key-from-rsa-to-der-for-javamail
	 */
	static private byte[] decodeRsaPKCS1PrivateKey(byte[] oldder) {
		final byte[] prefix = {0x30,(byte)0x82,0,0, 2,1,0, // SEQUENCE(lenTBD) and version INTEGER
			0x30,0x0d, 6,9,0x2a,(byte)0x86,0x48,(byte)0x86,(byte)0xf7,0x0d,1,1,1, 5,0, // AlgID for rsaEncryption,NULL
			4,(byte)0x82,0,0 }; // OCTETSTRING(lenTBD)
		byte[] newder = new byte [prefix.length + oldder.length];
		System.arraycopy (prefix,0, newder,0, prefix.length);
		System.arraycopy (oldder,0, newder,prefix.length, oldder.length);
		// and patch the (variable) lengths to be correct
		int len = oldder.length, loc = prefix.length-2;
		newder[loc] = (byte)(len>>8); newder[loc+1] = (byte)len;
		len = newder.length-4; loc = 2;
		newder[loc] = (byte)(len>>8); newder[loc+1] = (byte)len;

		return newder;
	}

	static private final byte[] readPemFormat(String in) {
		StringBuilder sb = new StringBuilder();
		for(String line: new LineIterator(in)) {
			if(! line.startsWith("--") && line.length() > 0) {
				sb.append(line);

			}
		}
		return StringTool.decodeBase64(sb.toString());
	}

	/**
	 * See
	 * http://techxperiment.blogspot.com/2016/10/create-and-read-pkcs-8-format-private.html
	 */
	static public String encodeSshPkcs8PrivateKey(PrivateKey key) throws Exception {
		JcaPKCS8Generator g = new JcaPKCS8Generator(key, null);
		PemObject pem = g.generate();
		StringWriter sw = new StringWriter();
		try(JcaPEMWriter pw = new JcaPEMWriter(sw)) {
			pw.writeObject(pem);
		}
		return sw.toString();
	}

	public final static class RSAPublicKeyImpl implements RSAPublicKey {
		private final BigInteger m_publicExp;

		private final BigInteger m_modulo;

		private final String m_algo;
		private final String m_format;
		private final byte[] m_encoded;

		public RSAPublicKeyImpl(BigInteger publicExp, BigInteger modulo, String algo, String format, byte[] encoded) {
			m_publicExp = publicExp;
			m_modulo = modulo;
			m_algo = algo;
			m_format = format;
			m_encoded = encoded;
		}

		@Override public BigInteger getPublicExponent() {
			return m_publicExp;
		}

		@Override public String getAlgorithm() {
			return m_algo;
		}

		@Override public String getFormat() {
			return m_format;
		}

		@Override public byte[] getEncoded() {
			return m_encoded;
		}

		@Override public BigInteger getModulus() {
			return m_modulo;
		}
	}


}
