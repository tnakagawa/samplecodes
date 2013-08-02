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
 * プロパティファイル暗号／複合サンプル
 * 
 * @author nakagawa
 */
public class SampleCipher {
	
	/** 暗号／複合アルゴリズム */
	private static final String TRANSFORMATION = "Blowfish";
	
	/** ハッシュアルゴリズム */
	private static final String ALGORITHM = "MD5";

	/**
	 * プロパティファイル読込
	 * 
	 * @param file 暗号プロパティファイル
	 * @param key 複合キー
	 * @return 複合プロパティファイル
	 * @throws IOException 例外
	 */
	public static Properties loadCryptProp(File file, String key) throws IOException {
		Properties prop = null;
		// 暗号プロパティファイル存在チェック
		if (file.exists() && file.isFile()) {
			CipherInputStream cis = null;
			FileInputStream fis = null;
			try {
				// 複合キー生成
				Cipher cipher = Cipher.getInstance(TRANSFORMATION);
				Key skey = new SecretKeySpec(getHash(key), TRANSFORMATION);
				cipher.init(Cipher.DECRYPT_MODE, skey);
				// プロパティ生成
				prop = new Properties();
				// 複合処理
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
	 * プロパティファイル保存
	 * 
	 * @param prop 保存プロパティファイル
	 * @param comments プロパティコメント
	 * @param file 保存ファイル
	 * @param key 暗号キー
	 * @throws IOException 例外
	 */
	public static void storeCryptProp(Properties prop, String comments, File file, String key) throws IOException {
		CipherOutputStream cos = null;
		FileOutputStream fos = null;
		try {
			// 暗号キー生成
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			Key skey = new SecretKeySpec(getHash(key), TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			fos = new FileOutputStream(file, false);
			cos = new CipherOutputStream(fos, cipher);
			// 暗号処理
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
	 * ハッシュ値取得
	 * 
	 * @param string 対処文字列
	 * @return ハッシュ
	 * @throws Exception 例外
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
