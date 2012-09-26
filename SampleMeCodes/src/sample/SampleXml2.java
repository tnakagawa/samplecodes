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
 * JavaのMapオブジェクトをXMLに変換するユーティリティ
 * 
 * @author osgi
 */
public class SampleXml2 {

	/** エンコード */
	public static final String ENCODE = "UTF-8";

	/** 改行 */
	private static final String CRLF = "\r\n";

	/** XMLヘッダサニタイズ */
	private static final String[][] HEAD_SANITIZES = {{"/", "" }, {" ", "" }, {":", "" }, };

	/** XMLヘッダ先頭禁止文字列 */
	private static final char[] NUMS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', };

	/** XMLサニタイズ */
	private static final String[][] SANITIZES = {
		{"&", "&amp;" }, {"\r", "" }, {"\n", "" }, {"\t", "" }, {"<", "&lt;" }, {">", "&gt;" },
		// { "'", "&apos;" },
		{"\"", "&quot;" },  };

	/** XMLヘッダ */
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
	 * XML記述
	 * 
	 * @param data データ
	 * @param os 出力先
	 * @throws IOException 例外
	 */
	public static String object2xml(String root, Object data) {
		return XML_HEAD + object2xml(0, root, data);
	}

	/**
	 * XMLヘッダサニタイズ null文字を空文字に変更
	 * 
	 * @param object 対象オブジェクト
	 * @return サニタイズ後文字列
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
	 * XML出力
	 * 
	 * @param indent インデント数
	 * @param head ヘッダ文字列
	 * @param value 値
	 * @param os 出力先
	 * @throws IOException 例外
	 */
	private static String object2xml(int indent, String head, Object value) {
		StringBuffer xml = new StringBuffer();
		
		// インデント生成
		StringBuffer tab = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			tab.append("\t");
		}
		
		// ヘッダ文字列生成
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
			// nullの場合
			xml.append(tab.toString() + "<" + headStr + " type=\"value\" null=\"true\"/>" + CRLF);
		} else if (value instanceof Map) {
			// Map型の場合、<key>value</key>の形に変更
			xml.append(tab.toString() + "<" + headStr + " type=\"map\">" + CRLF);
			Map map = (Map) value;
			Iterator it = map.keySet().iterator();
			Object key = null;
			// キー分ループ
			while (it.hasNext()) {
				key = it.next();
				xml.append(object2xml(indent + 1, headSanitize(key), map.get(key)));
			}
			xml.append(tab.toString() + "</" + headStr + ">" + CRLF);
		} else if (value.getClass().isArray()) {
			// 配列の場合、配列分ループ
			for (int i = 0; i < Array.getLength(value); i++) {
				xml.append(object2xml(indent, headStr, Array.get(value, i)));
			}
		} else if (value instanceof Collection) {
			// java.util.Collectionも配列として対応
			// Collectionを配列に変更して、再帰呼び出し
			Collection collection = (Collection) value;
			Object[] objects = new Object[collection.size()];
			objects = collection.toArray(objects);
			xml.append(object2xml(indent, head, objects));
		} else {
			// 文字列、その他はすべて文字列扱いとする
			xml.append(tab.toString() + "<" + headStr + " type=\"value\">" + sanitize(value) + "</" + headStr + ">" + CRLF);
		}
		
		return xml.toString();
	}

	/**
	 * 文字列置換 {@link RestUtil#replaceAll(String, String, String, int)}
	 * の開始位置を0としたもの
	 * ※：CDCFPには、String#replaceAllが存在しないので、自作
	 * 
	 * @param string 対象文字列
	 * @param target 置換元文字列
	 * @param replace 置換後文字列
	 * @return 置換後文字列
	 */
	private static String replaceAll(String string, String target, String replace) {
		return replaceAll(string, target, replace, 0);
	}

	
	/**
	 * 文字列置換
	 * ※：CDCFPには、String#replaceAllが存在しないので、自作
	 * 
	 * @param string 対象文字列
	 * @param target 置換元文字列
	 * @param replace 置換後文字列
	 * @param index 置換開始位置
	 * @return 置換後文字列
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
	 * サニタイズ null文字を空文字に変更
	 * 
	 * @param object 対象オブジェクト
	 * @return サニタイズ後文字列
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
