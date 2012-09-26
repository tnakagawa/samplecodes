package sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link Runtime#exec(String[])�p���[�e�B���e�B
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

	/** �f�t�H���g�G���R�[�f�B���O */
	public static final String DEFAULT_ENC = System.getProperty("file.encoding");
	
	/** �L�[�i���ʁA�I���R�[�h�j */
	public static final String KEY_RESULT = "result";

	/** �L�[�i�W���o�́j */
	public static final String KEY_SDTOUT = "stdout";

	/** �L�[�i�W���G���[�o�́j */
	public static final String KEY_SDTERR = "stderr";

	/** �o�b�t�@�T�C�Y */
	private static final int BUF_SIZE = 1024;

	/**
	 * Exec���s
	 * 
	 * @param cmdarray �R�}���h�z��
	 * @return ����
	 * @throws IOException ��O
	 * @throws InterruptedException ��O
	 */
	public static Map exec(String[] cmdarray) throws IOException, InterruptedException {
		return exec(cmdarray, DEFAULT_ENC, 0);
	}

	/**
	 * Exec���s
	 * 
	 * @param cmdarray �R�}���h�z��
	 * @param timeout �^�C���A�E�g�l
	 * @return ����
	 * @throws IOException ��O
	 * @throws InterruptedException ��O
	 */
	public static Map exec(String[] cmdarray, long timeout) throws IOException, InterruptedException {
		return exec(cmdarray, DEFAULT_ENC, timeout);
	}

	/**
	 * Exec���s
	 * 
	 * @param cmdarray �R�}���h�z��
	 * @param enc �G���R�[�f�B���O
	 * @return ����
	 * @throws IOException ��O
	 * @throws InterruptedException ��O
	 */
	public static Map exec(String[] cmdarray, String enc) throws IOException, InterruptedException {
		return exec(cmdarray, enc, 0);
	}
	
	/**
	 * Exec���s
	 * 
	 * @param cmdarray �R�}���h�z��
	 * @param enc �G���R�[�f�B���O
	 * @param timeout �^�C���A�E�g�l
	 * @return ����
	 * @throws IOException ��O
	 * @throws InterruptedException ��O
	 */
	public static Map exec(String[] cmdarray, String enc, long timeout) throws IOException, InterruptedException {
		// �߂�l�pMap
		Map map = null;
		// �v���Z�X
		Process process = null;
		// �W���o��Stream
		InputStream stdoutInputStream = null;
		// �W���G���[�o��Stream
		InputStream stderrInputStream = null;
		// �W������Stream
		OutputStream outputStream = null;
		try {
			// ���s
			process = Runtime.getRuntime().exec(cmdarray);
			
			// �W���o�͓ǂݎ��J�n
			stdoutInputStream = process.getInputStream();
			SampleRuntimeExec stdout = new SampleRuntimeExec(stdoutInputStream);
			Thread stdoutThread = new Thread(stdout, "Process#getInputStream>Thread");
			stdoutThread.start();
			// �W���G���[�ǂݎ��J�n
			stderrInputStream = process.getErrorStream();
			SampleRuntimeExec stderr = new SampleRuntimeExec(stderrInputStream);
			Thread stderrThread = new Thread(stderr, "Process#getErrorStream>Thread");
			stderrThread.start();

			// �X���b�h�҂����킹
			stdoutThread.join(Math.round((double) timeout / 2));
			stderrThread.join(Math.round((double) timeout / 2));
			
			// �^�C���A�E�g����
			boolean timeoutFlg = false;
			if (stdoutThread.isAlive() || stderrThread.isAlive()) {
				timeoutFlg = true;
			}
			
			// ����
			map = new LinkedHashMap();
			// �I���R�[�h�ݒ�
			if (timeoutFlg) {
				map.put(KEY_RESULT, null);
			} else {
				map.put(KEY_RESULT, new Integer(process.exitValue()));
			}
			// �W���o�͐ݒ�
			map.put(KEY_SDTOUT, new String(stdout.baos.toByteArray(), enc));
			// �W���G���[�ݒ�
			map.put(KEY_SDTERR, new String(stderr.baos.toByteArray(), enc));

			// �W������Stream
			outputStream = process.getOutputStream();
		} finally {
			// �W���o�̓N���[�Y����
			if (stdoutInputStream != null) {
				try {
					stdoutInputStream.close();
				} catch (IOException e) {
					// ��O���������Ă��Ώ��̂��悤���Ȃ��̂ŁA���O�̂ݏo��
					e.printStackTrace();
				}
			}
			// �W���G���[�o�̓N���[�Y����
			if (stderrInputStream != null) {
				try {
					stderrInputStream.close();
				} catch (IOException e) {
					// ��O���������Ă��Ώ��̂��悤���Ȃ��̂ŁA���O�̂ݏo��
					e.printStackTrace();
				}
			}
			// �W�����̓N���[�Y����
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					// ��O���������Ă��Ώ��̂��悤���Ȃ��̂ŁA���O�̂ݏo��
					e.printStackTrace();
				}
			}
			// �v���Z�X��~����
			if (process != null) {
				process.destroy();
			}
		}
		return map;
	}
	
	/** ��������Stram */
	private ByteArrayOutputStream baos = null;

	/** �ǂݍ���Stream */
	private InputStream inputStream = null;
	
	/**
	 * �R���X�g���N�^
	 * 
	 * @param inputStream �ǂݍ���Stream
	 */
	private SampleRuntimeExec(InputStream inputStream) {
		this.inputStream = inputStream;
		// ������
		baos = new ByteArrayOutputStream();
	}
	
	/**
	 * @see Runnable#run()
	 */
	public void run() {
		if (inputStream != null) {
			try {
				// �擾�o�C�g��
				int len = 0;
				// �o�b�t�@
				byte[] buf = new byte[BUF_SIZE];
				// �擾���[�v
				while (true) {
					// �o�b�t�@�ǂݎ��
					len = inputStream.read(buf);
					// �ǂݎ�蔻��
					if (len >= 0) {
						// �ǂݎ�ꂽ�̂ŁA��������
						baos.write(buf, 0, len);
						baos.flush();
					} else {
						// �ǂݎ��Ȃ������̂ŁA�I��
						break;
					}
				}
				baos.flush();
			} catch (Exception e) {
				// ��O�A��������ƌ����s��
				e.printStackTrace();
			} finally {
				try {
					// �N���[�Y
					baos.close();
				} catch (IOException e) {
					// ��O�A��������ƌ����s��
					e.printStackTrace();
				}
			}
		}
	}

}
