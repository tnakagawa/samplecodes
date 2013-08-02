package sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 * �v���p�e�B�t�@�C���Í��^�����T���v��
 * 
 * @author nakagawa
 */
public class SampleCipher {
	
	/** �Í��^�����A���S���Y�� */
	private static final String TRANSFORMATION = "Blowfish";
	
	/** �n�b�V���A���S���Y�� */
	private static final String ALGORITHM = "MD5";

	/**
	 * �v���p�e�B�t�@�C���Ǎ�
	 * 
	 * @param file �Í��v���p�e�B�t�@�C��
	 * @param key �����L�[
	 * @return �����v���p�e�B�t�@�C��
	 * @throws IOException ��O
	 */
	public static Properties loadCryptProp(File file, String key) throws IOException {
		Properties prop = null;
		// �Í��v���p�e�B�t�@�C�����݃`�F�b�N
		if (file.exists() && file.isFile()) {
			CipherInputStream cis = null;
			FileInputStream fis = null;
			try {
				// �����L�[����
				Cipher cipher = Cipher.getInstance(TRANSFORMATION);
				Key skey = new SecretKeySpec(getHash(key), TRANSFORMATION);
				cipher.init(Cipher.DECRYPT_MODE, skey);
				// �v���p�e�B����
				prop = new Properties();
				// ��������
				fis = new FileInputStream(file);
				cis = new CipherInputStream(fis, cipher);
				prop.load(cis);
			} catch (Exception e) {
				e.printStackTrace();
				prop = null;
				IOException ie = new IOException("loadCryptProp Exception:" + e.getMessage());
				ie.setStackTrace(e.getStackTrace());
				throw ie;
			} finally {
				if (cis != null) {
					try {
						cis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return prop;
	}

	/**
	 * �v���p�e�B�t�@�C���ۑ�
	 * 
	 * @param prop �ۑ��v���p�e�B�t�@�C��
	 * @param comments �v���p�e�B�R�����g
	 * @param file �ۑ��t�@�C��
	 * @param key �Í��L�[
	 * @throws IOException ��O
	 */
	public static void storeCryptProp(Properties prop, String comments, File file, String key) throws IOException {
		CipherOutputStream cos = null;
		FileOutputStream fos = null;
		try {
			// �Í��L�[����
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			Key skey = new SecretKeySpec(getHash(key), TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			fos = new FileOutputStream(file, false);
			cos = new CipherOutputStream(fos, cipher);
			// �Í�����
			prop.store(cos, comments);
		} catch (Exception e) {
			e.printStackTrace();
			prop = null;
			IOException ie = new IOException("storeCryptProp Exception:" + e.getMessage());
			ie.setStackTrace(e.getStackTrace());
			throw ie;
		} finally {
			if (cos != null) {
				try {
					cos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * �n�b�V���l�擾
	 * 
	 * @param string �Ώ�������
	 * @return �n�b�V��
	 * @throws Exception ��O
	 */
	private static byte[] getHash(String string) throws Exception {
		byte[] bs = null;
		MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
		digest.update(string.getBytes());
		bs = digest.digest();
		return bs;
	}

	// ================================================================

	public static void main(String[] args) {
		try {
			File file = new File("C:\\tmp\\hoge.properties");
			String key = "password";
			Properties prop = null;
			prop = SampleCipher.loadCryptProp(file, key);
			if (prop != null) {
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
				prop.list(System.out);
				prop.clear();
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
			} else {
				prop = new Properties();
			}
			prop.setProperty("aaaaaa", "123123");
			prop.setProperty("bbbbbb", "555666");
			prop.setProperty("cccccc", "999888");
			prop.setProperty("date", "" + new Date());
			SampleCipher.storeCryptProp(prop, null, file, key);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
