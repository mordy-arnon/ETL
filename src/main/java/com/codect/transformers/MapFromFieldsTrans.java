package com.codect.transformers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * add json column which contain one or more column data.
 * for example:
 * _id:{
 * 		"name":"a",
 * 		"id":"111"
 * 	}
 * 
 * config:
 * {
 "class" : "MapFromFieldsTrans",
 "fields" : ["name","id"],
 "to":"_id"
 }
 */
public class MapFromFieldsTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		List<String> fields = (List<String>) conf.get("fields");
		Map<String, Object> id = new HashMap<String, Object>();
		for (String col : fields) {
			id.put(col, line.get(col));
		}
		line.put((String) conf.get("to"), id);
		return line;
	}

}
