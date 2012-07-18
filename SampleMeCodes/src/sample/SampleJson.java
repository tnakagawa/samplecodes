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
	 * オブジェクトをJSON文字列へ変換
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
			// 初回フラグ
			boolean flg = true;
			// 名前用変数
			Object string = null;
			// 値用変数
			Object value = null;
			// 文字列
			StringBuffer buffer = new StringBuffer();
			// オブジェクトは、{(左の中括弧)で始まる
			buffer.append("{");
			// 名前数分ループ
			while (it.hasNext()) {
				// 初回判定
				if (flg) {
					// 初回の場合
					flg = false;
				} else {
					// 「, 」で連結
					buffer.append(", ");
				}
				// 名前取得
				string = it.next();
				// 値取得
				value = object.get(string);
				// 名前＋「: 」＋値で設定
				buffer.append(string2json(string.toString()) + ": " + value2json(value));
			}
			// オブジェクトは、} (右の中括弧)で終わる
			buffer.append("}");
			// 文字列を設定
			ret = buffer.toString();
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
		// null判定
		if (string != null) {
			ret = string;
			// サニタイズ
			for (int i = 0; i < SANITIZES.length; i++) {
				ret = replaceAll(ret, SANITIZES[i][0], SANITIZES[i][1]);
			}
		}
		return ret;
	}
	
	/**
	 * 文字列をJSON文字列へ変換
	 * 
	 * @param value 対象文字列
	 * @return JSON文字列
	 */
	private static String string2json(String value) {
		// 文字列は、2重引用符で囲われてバックスラッシュエスケープされたゼロ文字以上のユニコード文字の集まりです。
		return "\"" + sanitize(value) + "\"";
	};

	/**
	 * 値をJSON文字列へ変換
	 * 
	 * @param value 対象オブジェクト
	 * @return JSON型文字列
	 */
	private static String value2json(Object value) {
		String ret = null;
		if (value == null) {
			// nullの場合
			ret = "null";
		} else if (value instanceof Boolean
				|| value instanceof Integer
				|| value instanceof Double
				|| value instanceof Float) {
			// 数値、true、falseの場合
			ret = value.toString();
		} else if (value instanceof Map) {
			// オブジェクトの場合
			ret = object2json((Map) value);
		} else if (value.getClass().isArray()) {
			// 配列の場合
			// 文字列
			StringBuffer buffer = new StringBuffer();
			// 配列は、[(左の大括弧)で始まる
			buffer.append("[");
			// 配列数分ループ
			for (int i = 0; i < Array.getLength(value); i++) {
				// 初回判定
				if (i != 0) {
					// 値は、，(コンマ)で区切られる
					buffer.append(", ");
				}
				// 値追加
				buffer.append(value2json(Array.get(value, i)));
			}
			// 配列は、](右の大括弧)で終わる
			buffer.append("]");
			// 文字列をせて値
			ret = buffer.toString();
		} else if (value instanceof Collection) {
			// java.util.Collectionも配列として対応
			// Collectionを配列に変更して、再帰呼び出し
			Collection collection = (Collection) value;
			Object[] objects = new Object[collection.size()];
			objects = collection.toArray(objects);
			ret = value2json(objects);
		} else if (value instanceof Date) {
			// 日付の場合
			// JSONでは日付の指定がない為、フォーマットは独自
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ret = string2json(sdf.format((Date) value));
		} else {
			// 文字列、その他はすべて文字列扱いとする
			ret = string2json(value.toString());
		}
		return ret;
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

}
