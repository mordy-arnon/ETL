package com.codect.readers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codect.common.Fields;
import com.codect.connections.CloudantConnection;

public class CloudantReader extends Reader {
	private int pointer = 0;
	private int batchSize;
	private List<Object> find;

	@Override
	public boolean hasNext() {
		return find.size() > 0;
	}

	@Override
	public List<Map<String, Object>> next() {
		pointer +=find.size();
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		for (Object map : find) {
			ret.add((Map<String, Object>) map);
		}
		Map<String, Object> query = (Map<String, Object>) source.get(Fields.query);
		find = CloudantConnection.getInstance().find("fact_db", fixDollars(query),
				(List<String>) source.get(Fields.filter), batchSize, pointer);
		return ret;
	}

	private Map<String, Object> fixDollars(Map<String, Object> query) {
		Map<String, Object> fixedQuery = new HashMap<String, Object>();
		for (String key : query.keySet()) {
			Object object = query.get(key);
			if (object instanceof Map)
				object = fixDollars((Map<String, Object>) object);
			fixedQuery.put(key.replaceAll("\\&", "\\$"), object);
		}
		return fixedQuery;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void init() {
		batchSize = ((Number) source.get(Fields.batchSize)).intValue();
		if (batchSize == 0)
			batchSize = 1;
		Map<String, Object> query = (Map<String, Object>) source.get(Fields.query);
		query.put("TableName", (String) source.get(Fields.sourceTable));
		find = CloudantConnection.getInstance().find("fact_db", fixDollars(query),
				(List<String>) source.get(Fields.filter), batchSize, pointer);
	}
	@Override
	public void remove() {
	}
}
