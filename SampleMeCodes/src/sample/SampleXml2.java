package sample;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java��Map�I�u�W�F�N�g��XML�ɕϊ����郆�[�e�B���e�B
 * 
 * @author osgi
 */
public class SampleXml2 {

	/** �G���R�[�h */
	public static final String ENCODE = "UTF-8";

	/** ���s */
	private static final String CRLF = "\r\n";

	/** XML�w�b�_�T�j�^�C�Y */
	private static final String[][] HEAD_SANITIZES = {{"/", "" }, {" ", "" }, {":", "" }, };

	/** XML�w�b�_�擪�֎~������ */
	private static final char[] NUMS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', };

	/** XML�T�j�^�C�Y */
	private static final String[][] SANITIZES = {
		{"&", "&amp;" }, {"\r", "" }, {"\n", "" }, {"\t", "" }, {"<", "&lt;" }, {">", "&gt;" },
		// { "'", "&apos;" },
		{"\"", "&quot;" },  };

	/** XML�w�b�_ */
	private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + CRLF;
	
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
		String[] strings = {"+++", "---", "***", "///"};
		map.put("iii", strings);
		map.put("jjj", null);
		System.out.println(object2xml("root", map));
	}
	
	/**
	 * XML�L�q
	 * 
	 * @param data �f�[�^
	 * @param os �o�͐�
	 * @throws IOException ��O
	 */
	public static String object2xml(String root, Object data) {
		return XML_HEAD + object2xml(0, root, data);
	}

	/**
	 * XML�w�b�_�T�j�^�C�Y null�������󕶎��ɕύX
	 * 
	 * @param object �ΏۃI�u�W�F�N�g
	 * @return �T�j�^�C�Y�㕶����
	 */
	private static String headSanitize(Object object) {
		String ret = "";
		if (object != null) {
			ret = object.toString();
			for (int i = 0; i < HEAD_SANITIZES.length; i++) {
				ret = replaceAll(ret, HEAD_SANITIZES[i][0], HEAD_SANITIZES[i][1]);
			}
		}
		return ret;
	}

	/**
	 * XML�o��
	 * 
	 * @param indent �C���f���g��
	 * @param head �w�b�_������
	 * @param value �l
	 * @param os �o�͐�
	 * @throws IOException ��O
	 */
	private static String object2xml(int indent, String head, Object value) {
		StringBuffer xml = new StringBuffer();
		
		// �C���f���g����
		StringBuffer tab = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			tab.append("\t");
		}
		
		// �w�b�_�����񐶐�
		String headStr = head;
		if (headStr.length() > 0) {
			for (int i = 0; i < NUMS.length; i++) {
				if (headStr.charAt(0) == NUMS[i]) {
					headStr = "n" + headStr;
					break;
				}
			}
		}
		
		if (value == null) {
			// null�̏ꍇ
			xml.append(tab.toString() + "<" + headStr + " type=\"value\" null=\"true\"/>" + CRLF);
		} else if (value instanceof Map) {
			// Map�^�̏ꍇ�A<key>value</key>�̌`�ɕύX
			xml.append(tab.toString() + "<" + headStr + " type=\"map\">" + CRLF);
			Map map = (Map) value;
			Iterator it = map.keySet().iterator();
			Object key = null;
			// �L�[�����[�v
			while (it.hasNext()) {
				key = it.next();
				xml.append(object2xml(indent + 1, headSanitize(key), map.get(key)));
			}
			xml.append(tab.toString() + "</" + headStr + ">" + CRLF);
		} else if (value.getClass().isArray()) {
			// �z��̏ꍇ�A�z�񕪃��[�v
			for (int i = 0; i < Array.getLength(value); i++) {
				xml.append(object2xml(indent, headStr, Array.get(value, i)));
			}
		} else if (value instanceof Collection) {
			// java.util.Collection���z��Ƃ��đΉ�
			// Collection��z��ɕύX���āA�ċA�Ăяo��
			Collection collection = (Collection) value;
			Object[] objects = new Object[collection.size()];
			objects = collection.toArray(objects);
			xml.append(object2xml(indent, head, objects));
		} else {
			// ������A���̑��͂��ׂĕ����񈵂��Ƃ���
			xml.append(tab.toString() + "<" + headStr + " type=\"value\">" + sanitize(value) + "</" + headStr + ">" + CRLF);
		}
		
		return xml.toString();
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

	/**
	 * �T�j�^�C�Y null�������󕶎��ɕύX
	 * 
	 * @param object �ΏۃI�u�W�F�N�g
	 * @return �T�j�^�C�Y�㕶����
	 */
	private static String sanitize(Object object) {
		String ret = "";
		if (object != null) {
			ret = object.toString();
			for (int i = 0; i < SANITIZES.length; i++) {
				ret = replaceAll(ret, SANITIZES[i][0], SANITIZES[i][1]);
			}
		}
		return ret;
	}
}
