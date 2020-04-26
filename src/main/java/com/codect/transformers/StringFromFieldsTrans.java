package com.codect.transformers;

import java.util.List;
import java.util.Map;

/*
 * add json column which contain one or more column data.
 * config for example:
 * {
 *     "class" : "StringFromFieldsTrans",
 *     "fields" : ["name","|","id"],
 *     "to":"_id"
 * }
 */
public class StringFromFieldsTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		List<String> fields = (List<String>) conf.get("fields");
		String id = "";
		for (String col : fields) {
			object data=DBObjectUtil.getInnerField(col,line);
			if (data!=null)
				id+=data;
			else 
				id+=col;
		}
		line.put((String) conf.get("to"), id);
		return line;
	}

}
