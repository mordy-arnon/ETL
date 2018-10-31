package com.codect.transformers;

import java.util.Map;
/*
 * replace the name of the column.
 *   {
      "class" : "ShortRenameTrans",
      "list" : {
          "<from_name>":"<to_name>"
          }
      }
 */
public class ShortRenameTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) conf.get("list");
		for (String fromField : map.keySet()) {
			Object value = line.remove(fromField);
			line.put(map.get(fromField).toString(), value);
		}
		return line;
	}

}
