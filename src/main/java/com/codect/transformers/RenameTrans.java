package com.codect.transformers;

import java.util.List;
import java.util.Map;

import com.codect.common.DBObjectUtil;
/*
 * replace the name of the column.
 *   {
      "class" : "RenameTrans",
      "list" : [{
          "from":"<from_name>",
          "to":"<to_name>"
          }]
      }
 */
public class RenameTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) conf.get("list");
		for (Map<String, Object> map : list) {
			String fromField = (String) map.get("from");
			String toField = (String) map.get("to");
			Object value = DBObjectUtil.getInnerField(fromField, line);
			DBObjectUtil.deleteByCursor(line,fromField,true);
			DBObjectUtil.recursivePut(line, toField, value);
		}
		return line;
	}
}
