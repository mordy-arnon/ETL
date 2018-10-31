package com.codect.transformers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AddFieldTrans extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) conf.get("list");
		for (Map<String, Object> map : list) {
			String fromField = (String) map.get("fieldName");
			String value = (String) map.get("value");

			line.put(fromField, toValue(value,line));
		}
		return line;
	}

    private Object toValue(String value, Map<String, Object> line) {
        if ("->date".equals(value))
            return new SimpleDateFormat("yyyyMM").format(new Date());
        else if ("->datetime".equals(value))
            return new Date();
        else if (value.startsWith("#"))
        {
        	String key = value.substring(1);
        	if (line.containsKey(key))
        	    return line.get(key);
        }
        else if (value.startsWith("+"))
        {
        	String key = value.substring(1);
        	return key;
        }
        return null;
    }
}
