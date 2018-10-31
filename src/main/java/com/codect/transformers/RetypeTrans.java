package com.codect.transformers;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
/*
 * change column type.
 *   {
      "class" : "RetypeTrans",
      "list" : {
          "fieldName":"<name>",
          "type":"double"
          }
      }
 */
public class RetypeTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = ((List<Map<String, Object>>) conf.get("list"));
		for (Map<String, Object> map : list) {
			String fieldName = (String) map.get("fieldName");
			String type = (String) map.get("type");
			Object value = line.get(fieldName);
			value = changeValue(type, value);
			line.put(fieldName, value);
		}
		return line;
	}

	private Object changeValue(String type, Object value) {
		try {
			type = type.toLowerCase();
			if (type.startsWith("date:")){ //date:yyyyMMdd
				return new SimpleDateFormat(type.substring(5)).parse(""+value);
			}
			switch (type) {
			case "double":
				return Double.parseDouble("" + value);
			case "integer":
				return Integer.parseInt("" + value);
			case "long":
				return Long.parseLong("" + value);
			case "string":
				return "" + value;
			default:
				return value;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
}
