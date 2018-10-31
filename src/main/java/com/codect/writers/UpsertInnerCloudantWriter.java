package com.codect.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.codect.common.Fields;
import com.codect.connections.CloudantConnection;

public class UpsertInnerCloudantWriter extends Writer {
	private String tableName;
	private List<String> keys;
	private String parentKey;
	private String action;
	@Override
	public void close() throws IOException {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		tableName = (String) target.get(Fields.writeToTable);
		keys = (List<String>) target.get(Fields.keys);
		parentKey = (String) target.get(Fields.parentKey);
		action = (String) target.get(Fields.action);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void write(List<Map<String, Object>> next) {
		for (Map<String, Object> map : next) {
			map.put("TableName", tableName);
		}
		CloudantConnection connection = CloudantConnection.getInstance();
		for (Map<String, Object> line : next) {
			JSONObject query = new JSONObject();
			for (String key : keys) {
				query.put(key, line.get(key));
			}
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			List<Object> find = connection.find("fact_db", query, null);
			if (null != find && find.size() > 0)
				for (Object object : find) {
					Map<String, Object> obj = (Map<String, Object>) object;
					insertToObject(line, obj);
					data.add(obj);
				}
			else{
				Map<String,Object> obj = new HashMap<String, Object>();
				insertToObject(line, obj);
				data.add(obj);
			}
			connection.update("fact_db", data);
		}
	}

	private void insertToObject(Map<String, Object> line, Map<String, Object> obj) {
		Object inner = obj.get(parentKey);
		if("$set".equals(action)){
			if(null == inner)
				obj.put(parentKey, line);
			else if(inner instanceof Map)
				((Map)inner).putAll(line);
		}
		else if("$push".equals(action))
			if(null == inner){
				ArrayList<Object> innerList = new ArrayList<Object>();
				innerList.add(line);
				obj.put(parentKey, innerList);
			}
			else if(inner instanceof List)
				((ArrayList<Object>)inner).add(line);
	}

	@Override
	public void prepareTarget(Map<String, Object> map) {
	}
}
