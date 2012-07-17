package sample;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 英語
 * http://www.json.org/
 * 日本語
 * http://www.json.org/json-ja.html
 * 
 * JavaのMapをJsonのobjectへ簡易的に変換するサンプル
 * 
 * @author tnakagawa
 */
public class SampleJson {

	/** サニタイズ文字列 */
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
	 * オブジェクトをJSON変換
	 * 
	 * @param object 対象オブジェクト
	 * @return JSON文字列
	 */
	public static String object2json(Map object) {
		String ret = null;
		// 対象オブジェクトのnullチェック
		if (object != null) {
			// key一覧取得
			Iterator it = object.keySet().iterator();
			// objectは(左の中括弧)で始まる
			ret = "{";
			// 初回フラグ
			boolean flg = true;
			// 名前用変数
			Object string = null;
			// 値用変数
			Object value = null;
			// 名前数分ループ
			while (it.hasNext()) {
				// 初回判定
				if (flg) {
					// 初回の場合、次回からは「, 」で連結
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
	 * 文字列置換 {@link RestUtil#replaceAll(String, String, String, int)}
	 * の開始位置を0としたもの
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
	 * サニタイズ
	 * 
	 * @param string 対象文字列
	 * @return サニタイズ後文字列
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
	 * 文字列をJSON変換
	 * 
	 * @param value 対象文字列
	 * @return JSON文字列
	 */
	private static String string2json(String value) {
		// 本当はサニタイズしないとダメ
		return "\"" + sanitize(value) + "\"";
	};

	/**
	 * オブジェクト→JSON変換
	 * 
	 * @param value 対象オブジェクト
	 * @return JSON型文字列
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
