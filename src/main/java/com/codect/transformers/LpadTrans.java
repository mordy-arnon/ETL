package com.codect.transformers;

import java.util.List;
import java.util.Map;

/*
 * pad string with "0" from right.
 * for example: "abc" -> "0000abc"
 * 
 * {
 * "class":"LpadTrans",
 "list":[
 {
 "fieldName":"<fieln_name>",
 "size":<target_field_size>>
 }
 ]
 * }
 */
public class LpadTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) conf.get("list");
		for (Map<String, Object> map : list) {
			String fromField = (String) map.get("fieldName");
			int size = ((Number) map.get("size")).intValue();
			Object value = line.get(fromField);
			while (value.toString().length() < size)
				value = "0" + value;
			line.put(fromField, value);
		}
		return line;
	}
}
