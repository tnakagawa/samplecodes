package sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link Runtime#exec(String[])｝ユーティリティ
 * 
 * @author nakagawa
 */
public final class SampleRuntimeExec implements Runnable {
	
	public static void main(String[] args) {
		try {
			Map ret = SampleRuntimeExec.exec(new String[] { "cmd", "/c", "arp",	"-a" });
			System.out.println("ret:" + ret);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** デフォルトエンコーディング */
	public static final String DEFAULT_ENC = System.getProperty("file.encoding");
	
	/** キー（結果、終了コード） */
	public static final String KEY_RESULT = "result";

	/** キー（標準出力） */
	public static final String KEY_SDTOUT = "stdout";

	/** キー（標準エラー出力） */
	public static final String KEY_SDTERR = "stderr";

	/** バッファサイズ */
	private static final int BUF_SIZE = 1024;

	/**
	 * Exec実行
	 * 
	 * @param cmdarray コマンド配列
	 * @return 結果
	 * @throws IOException 例外
	 * @throws InterruptedException 例外
	 */
	public static Map exec(String[] cmdarray) throws IOException, InterruptedException {
		return exec(cmdarray, DEFAULT_ENC, 0);
	}

	/**
	 * Exec実行
	 * 
	 * @param cmdarray コマンド配列
	 * @param timeout タイムアウト値
	 * @return 結果
	 * @throws IOException 例外
	 * @throws InterruptedException 例外
	 */
	public static Map exec(String[] cmdarray, long timeout) throws IOException, InterruptedException {
		return exec(cmdarray, DEFAULT_ENC, timeout);
	}

	/**
	 * Exec実行
	 * 
	 * @param cmdarray コマンド配列
	 * @param enc エンコーディング
	 * @return 結果
	 * @throws IOException 例外
	 * @throws InterruptedException 例外
	 */
	public static Map exec(String[] cmdarray, String enc) throws IOException, InterruptedException {
		return exec(cmdarray, enc, 0);
	}
	
	/**
	 * Exec実行
	 * 
	 * @param cmdarray コマンド配列
	 * @param enc エンコーディング
	 * @param timeout タイムアウト値
	 * @return 結果
	 * @throws IOException 例外
	 * @throws InterruptedException 例外
	 */
	public static Map exec(String[] cmdarray, String enc, long timeout) throws IOException, InterruptedException {
		// 戻り値用Map
		Map map = null;
		// プロセス
		Process process = null;
		// 標準出力Stream
		InputStream stdoutInputStream = null;
		// 標準エラー出力Stream
		InputStream stderrInputStream = null;
		// 標準入力Stream
		OutputStream outputStream = null;
		try {
			// 実行
			process = Runtime.getRuntime().exec(cmdarray);
			
			// 標準出力読み取り開始
			stdoutInputStream = process.getInputStream();
			SampleRuntimeExec stdout = new SampleRuntimeExec(stdoutInputStream);
			Thread stdoutThread = new Thread(stdout, "Process#getInputStream>Thread");
			stdoutThread.start();
			// 標準エラー読み取り開始
			stderrInputStream = process.getErrorStream();
			SampleRuntimeExec stderr = new SampleRuntimeExec(stderrInputStream);
			Thread stderrThread = new Thread(stderr, "Process#getErrorStream>Thread");
			stderrThread.start();

			// スレッド待ち合わせ
			stdoutThread.join(Math.round((double) timeout / 2));
			stderrThread.join(Math.round((double) timeout / 2));
			
			// タイムアウト判定
			boolean timeoutFlg = false;
			if (stdoutThread.isAlive() || stderrThread.isAlive()) {
				timeoutFlg = true;
			}
			
			// 結果
			map = new LinkedHashMap();
			// 終了コード設定
			if (timeoutFlg) {
				map.put(KEY_RESULT, null);
			} else {
				map.put(KEY_RESULT, new Integer(process.exitValue()));
			}
			// 標準出力設定
			map.put(KEY_SDTOUT, new String(stdout.baos.toByteArray(), enc));
			// 標準エラー設定
			map.put(KEY_SDTERR, new String(stderr.baos.toByteArray(), enc));

			// 標準入力Stream
			outputStream = process.getOutputStream();
		} finally {
			// 標準出力クローズ処理
			if (stdoutInputStream != null) {
				try {
					stdoutInputStream.close();
				} catch (IOException e) {
					// 例外が発生しても対処のしようがないので、ログのみ出力
					e.printStackTrace();
				}
			}
			// 標準エラー出力クローズ処理
			if (stderrInputStream != null) {
				try {
					stderrInputStream.close();
				} catch (IOException e) {
					// 例外が発生しても対処のしようがないので、ログのみ出力
					e.printStackTrace();
				}
			}
			// 標準入力クローズ処理
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					// 例外が発生しても対処のしようがないので、ログのみ出力
					e.printStackTrace();
				}
			}
			// プロセス停止処理
			if (process != null) {
				process.destroy();
			}
		}
		return map;
	}
	
	/** 書き込みStram */
	private ByteArrayOutputStream baos = null;

	/** 読み込みStream */
	private InputStream inputStream = null;
	
	/**
	 * コンストラクタ
	 * 
	 * @param inputStream 読み込みStream
	 */
	private SampleRuntimeExec(InputStream inputStream) {
		this.inputStream = inputStream;
		// 初期化
		baos = new ByteArrayOutputStream();
	}
	
	/**
	 * @see Runnable#run()
	 */
	public void run() {
		if (inputStream != null) {
			try {
				// 取得バイト列長
				int len = 0;
				// バッファ
				byte[] buf = new byte[BUF_SIZE];
				// 取得ループ
				while (true) {
					// バッファ読み取り
					len = inputStream.read(buf);
					// 読み取り判定
					if (len >= 0) {
						// 読み取れたので、書き込み
						baos.write(buf, 0, len);
						baos.flush();
					} else {
						// 読み取れなかったので、終了
						break;
					}
				}
				baos.flush();
			} catch (Exception e) {
				// 例外、発生すると原因不明
				e.printStackTrace();
			} finally {
				try {
					// クローズ
					baos.close();
				} catch (IOException e) {
					// 例外、発生すると原因不明
					e.printStackTrace();
				}
			}
		}
	}

}
