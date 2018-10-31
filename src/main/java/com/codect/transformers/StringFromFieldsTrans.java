package com.codect.transformers;

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
 "class" : "StringFromFieldsTrans",
 "fields" : ["name","id"],
 "to":"_id"
 }
 */
public class StringFromFieldsTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		List<String> fields = (List<String>) conf.get("fields");
		String id = "";
		for (String col : fields) {
			id+=line.get(col)+"|";
		}
		id=id.substring(0,id.length()-1);
		line.put((String) conf.get("to"), id);
		return line;
	}

}
