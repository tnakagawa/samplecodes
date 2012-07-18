package sample;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HTTPSアクセスによる、オレオレ証明書回避
 * 
 * @author osgi
 * 
 */
public class SampleHttps implements X509TrustManager, HostnameVerifier {

	public static void main(String[] args) {
		System.out.println("BEGIN");
		SampleHttps https = new SampleHttps();
		https.access();
		System.out.println("END");
	}

	private SampleHttps() {
		
	}
	
	private void access() {
		try {
			URL url = new URL("https://172.19.58.123/examples/");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			if (urlConnection instanceof HttpsURLConnection) {
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null, new TrustManager[]{this}, null);
				SSLSocketFactory sf = context.getSocketFactory();
				
				HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
				httpsURLConnection.setSSLSocketFactory(sf);
				httpsURLConnection.setHostnameVerifier(this);
			}
			
			//
			Map map = null;
			Object key = null;
			Iterator it = null;
			map = urlConnection.getRequestProperties();
			it = map.keySet().iterator();
			while (it.hasNext()) {
				key = it.next();
				System.out.println("" + key + " : " + map.get(key));
			}
			
			urlConnection.connect();
			
			urlConnection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see HostnameVerifier#verify(String, SSLSession)
	 */
	public boolean verify(String hostname, SSLSession session) {
		// サンプルなのでノーチェック
		System.out.println("HostnameVerifier#verify(" + hostname + "," + session + ")");
		return true;
	}

	/**
	 * @see X509TrustManager#checkClientTrusted(X509Certificate[], String)
	 */
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("X509TrustManager#checkClientTrusted>authType:" + authType);
		if (chain != null) {
			for (int i = 0; i < chain.length; i++) {
				System.out.println("X509TrustManager#checkClientTrusted>chain[" + i + "]:" + chain[i]);
			}
		}
	}

	/**
	 * @see X509TrustManager#checkServerTrusted(X509Certificate[], String)
	 */
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("X509TrustManager#checkServerTrusted>authType:" + authType);
		if (chain != null) {
			for (int i = 0; i < chain.length; i++) {
				System.out.println("X509TrustManager#checkServerTrusted>chain[" + i + "]:" + chain[i]);
			}
		}
	}

	/**
	 * @see X509TrustManager#getAcceptedIssuers()
	 */
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

}
