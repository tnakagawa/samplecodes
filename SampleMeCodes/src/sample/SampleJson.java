package sample;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * �p��
 * http://www.json.org/
 * ���{��
 * http://www.json.org/json-ja.html
 * 
 * Java��Map��Json��object�֊ȈՓI�ɕϊ�����T���v��
 * 
 * @author tnakagawa
 */
public class SampleJson {

	/** �T�j�^�C�Y������ */
	private static final String[][] SANITIZES = {
		{"\\", "\\\\" }, {"\"", "\\\"" }, {"/", "\\/" }, {"\b", "\\b" },
		{"\f", "\\f" }, {"\n", "\\n" }, {"\r", "\\r" }, {"\t", "\\t" }, };
	
	/**
	 * Test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Map map = new LinkedHashMap();
		map.put("aaa", null);
		map.put("bbb", Boolean.TRUE);
		map.put("ccc", new Integer(10));
		map.put("ddd", new Float(0.21));
		map.put("eee", new Double(5.43));
		map.put("fff", new int[]{1,2,3,4});
		map.put("ggg", new Date());
		List list = new ArrayList();
		list.add("111");
		list.add("222");
		list.add("333");
		map.put("hhh", list);
		System.out.println(object2json(map));
	}

	/**
	 * �I�u�W�F�N�g��JSON������֕ϊ�
	 * 
	 * @param object �ΏۃI�u�W�F�N�g
	 * @return JSON������
	 */
	public static String object2json(Map object) {
		String ret = null;
		// �ΏۃI�u�W�F�N�g��null�`�F�b�N
		if (object != null) {
			// key�ꗗ�擾
			Iterator it = object.keySet().iterator();
			// ����t���O
			boolean flg = true;
			// ���O�p�ϐ�
			Object string = null;
			// �l�p�ϐ�
			Object value = null;
			// ������
			StringBuffer buffer = new StringBuffer();
			// �I�u�W�F�N�g�́A{(���̒�����)�Ŏn�܂�
			buffer.append("{");
			// ���O�������[�v
			while (it.hasNext()) {
				// ���񔻒�
				if (flg) {
					// ����̏ꍇ
					flg = false;
				} else {
					// �u, �v�ŘA��
					buffer.append(", ");
				}
				// ���O�擾
				string = it.next();
				// �l�擾
				value = object.get(string);
				// ���O�{�u: �v�{�l�Őݒ�
				buffer.append(string2json(string.toString()) + ": " + value2json(value));
			}
			// �I�u�W�F�N�g�́A} (�E�̒�����)�ŏI���
			buffer.append("}");
			// �������ݒ�
			ret = buffer.toString();
		}
		return ret;
	}

	/**
	 * �T�j�^�C�Y
	 * 
	 * @param string �Ώە�����
	 * @return �T�j�^�C�Y�㕶����
	 */
	private static String sanitize(String string) {
		String ret = null;
		// null����
		if (string != null) {
			ret = string;
			// �T�j�^�C�Y
			for (int i = 0; i < SANITIZES.length; i++) {
				ret = replaceAll(ret, SANITIZES[i][0], SANITIZES[i][1]);
			}
		}
		return ret;
	}
	
	/**
	 * �������JSON������֕ϊ�
	 * 
	 * @param value �Ώە�����
	 * @return JSON������
	 */
	private static String string2json(String value) {
		// ������́A2�d���p���ň͂��ăo�b�N�X���b�V���G�X�P�[�v���ꂽ�[�������ȏ�̃��j�R�[�h�����̏W�܂�ł��B
		return "\"" + sanitize(value) + "\"";
	};

	/**
	 * �l��JSON������֕ϊ�
	 * 
	 * @param value �ΏۃI�u�W�F�N�g
	 * @return JSON�^������
	 */
	private static String value2json(Object value) {
		String ret = null;
		if (value == null) {
			// null�̏ꍇ
			ret = "null";
		} else if (value instanceof Boolean
				|| value instanceof Integer
				|| value instanceof Double
				|| value instanceof Float) {
			// ���l�Atrue�Afalse�̏ꍇ
			ret = value.toString();
		} else if (value instanceof Map) {
			// �I�u�W�F�N�g�̏ꍇ
			ret = object2json((Map) value);
		} else if (value.getClass().isArray()) {
			// �z��̏ꍇ
			// ������
			StringBuffer buffer = new StringBuffer();
			// �z��́A[(���̑劇��)�Ŏn�܂�
			buffer.append("[");
			// �z�񐔕����[�v
			for (int i = 0; i < Array.getLength(value); i++) {
				// ���񔻒�
				if (i != 0) {
					// �l�́A�C(�R���})�ŋ�؂���
					buffer.append(", ");
				}
				// �l�ǉ�
				buffer.append(value2json(Array.get(value, i)));
			}
			// �z��́A](�E�̑劇��)�ŏI���
			buffer.append("]");
			// ����������Ēl
			ret = buffer.toString();
		} else if (value instanceof Collection) {
			// java.util.Collection���z��Ƃ��đΉ�
			// Collection��z��ɕύX���āA�ċA�Ăяo��
			Collection collection = (Collection) value;
			Object[] objects = new Object[collection.size()];
			objects = collection.toArray(objects);
			ret = value2json(objects);
		} else if (value instanceof Date) {
			// ���t�̏ꍇ
			// JSON�ł͓��t�̎w�肪�Ȃ��ׁA�t�H�[�}�b�g�͓Ǝ�
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ret = string2json(sdf.format((Date) value));
		} else {
			// ������A���̑��͂��ׂĕ����񈵂��Ƃ���
			ret = string2json(value.toString());
		}
		return ret;
	}
	
	/**
	 * ������u�� {@link RestUtil#replaceAll(String, String, String, int)}
	 * �̊J�n�ʒu��0�Ƃ�������
	 * ���FCDCFP�ɂ́AString#replaceAll�����݂��Ȃ��̂ŁA����
	 * 
	 * @param string �Ώە�����
	 * @param target �u����������
	 * @param replace �u���㕶����
	 * @return �u���㕶����
	 */
	private static String replaceAll(String string, String target, String replace) {
		return replaceAll(string, target, replace, 0);
	}

	/**
	 * ������u��
	 * ���FCDCFP�ɂ́AString#replaceAll�����݂��Ȃ��̂ŁA����
	 * 
	 * @param string �Ώە�����
	 * @param target �u����������
	 * @param replace �u���㕶����
	 * @param index �u���J�n�ʒu
	 * @return �u���㕶����
	 */
	private static String replaceAll(String string, String target, String replace, int index) {
		String ret = string;
		if (ret != null && target != null && replace != null) {
			int idx = ret.indexOf(target, index);
			if (idx >= 0) {
				ret = ret.substring(0, idx) + replace + ret.substring(idx + target.length());
				ret = replaceAll(ret, target, replace, idx + replace.length());
			}
		}
		return ret;
	}

}
