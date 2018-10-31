package com.codect.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.codect.common.Fields;
import com.codect.connections.CloudantConnection;

public class UpsertCloudantWriter extends Writer {
	private String tableName;
	private List<String> keys;

	@Override
	public void close() throws IOException {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		tableName = (String) target.get(Fields.writeToTable);
		keys = (List<String>) target.get(Fields.keys);
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
					String parentKey = (String) target.get(Fields.parentKey);
					if (null != parentKey && !"".equals(parentKey))
						obj.put(parentKey, line);
					else
						obj.putAll(line);
					data.add(obj);
				}
			else
				data.add(line);
			connection.update("fact_db", data);
		}
	}

	@Override
	public void prepareTarget(Map<String, Object> map) {
	}
}
