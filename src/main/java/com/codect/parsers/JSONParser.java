package com.codect.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONParser extends Parser {


	@Override
	public List<Map<String, Object>> parse(Map<String, Object> source, String json) {
		JSONArray jsonArray = new JSONArray(json);
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		if (jsonArray.length() > 0) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject explrObject = (JSONObject) jsonArray.get(i);
				Map<String,Object> row = new HashMap<String, Object>();
				for (String key : explrObject.keySet()) {
					row.put(key, explrObject.get(key));
				}
				data.add(row);
			}
		}
		return data;
	}
}
