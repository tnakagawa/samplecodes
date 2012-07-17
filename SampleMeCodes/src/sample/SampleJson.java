package sample;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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

	}

	/**
	 * �I�u�W�F�N�g��JSON�ϊ�
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
			// object��(���̒�����)�Ŏn�܂�
			ret = "{";
			// ����t���O
			boolean flg = true;
			// ���O�p�ϐ�
			Object string = null;
			// �l�p�ϐ�
			Object value = null;
			// ���O�������[�v
			while (it.hasNext()) {
				// ���񔻒�
				if (flg) {
					// ����̏ꍇ�A���񂩂�́u, �v�ŘA��
					flg = false;
				} else {
					ret += ", ";
				}
				string = it.next();
				value = object.get(string);
				ret += string2json(string.toString()) + ": " + value2json(value);
				
			}
			ret += "}";
		}
		return ret;
	}

	/**
	 * ������u�� {@link RestUtil#replaceAll(String, String, String, int)}
	 * �̊J�n�ʒu��0�Ƃ�������
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
	
	/**
	 * �T�j�^�C�Y
	 * 
	 * @param string �Ώە�����
	 * @return �T�j�^�C�Y�㕶����
	 */
	private static String sanitize(String string) {
		String ret = null;
		if (string != null) {
			ret = string;
			for (int i = 0; i < SANITIZES.length; i++) {
				ret = replaceAll(ret, SANITIZES[i][0], SANITIZES[i][1]);
			}
		}
		return ret;
	}
	
	/**
	 * �������JSON�ϊ�
	 * 
	 * @param value �Ώە�����
	 * @return JSON������
	 */
	private static String string2json(String value) {
		// �{���̓T�j�^�C�Y���Ȃ��ƃ_��
		return "\"" + sanitize(value) + "\"";
	};

	/**
	 * �I�u�W�F�N�g��JSON�ϊ�
	 * 
	 * @param value �ΏۃI�u�W�F�N�g
	 * @return JSON�^������
	 */
	private static String value2json(Object value) {
		String ret = null;
		if (value == null) {
			ret = "null";
		} else if (value instanceof Boolean
				|| value instanceof Integer
				|| value instanceof Double
				|| value instanceof Float) {
			ret = value.toString();
		} else if (value instanceof Map) {
			// JSON Object
			ret = object2json((Map) value);
		} else if (value.getClass().isArray()) {
			// JSON array
			ret = "[";
			for (int i = 0; i < Array.getLength(value); i++) {
				if (i != 0) {
					ret += ", ";
				}
				ret += value2json(Array.get(value, i));
			}
			ret += "]";
		} else if (value instanceof List) {
			List list = (List) value;
			Object[] objects = new Object[list.size()];
			list.toArray(objects);
			ret = value2json(objects);
		} else if (value instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSS");
			ret = string2json(sdf.format((Date) value));
		} else {
			ret = string2json(value.toString());
		}
		return ret;
	}
}