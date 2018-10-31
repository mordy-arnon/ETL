package com.codect.writers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codect.common.Fields;
import com.codect.connections.ESConnection;

public class ESWriter extends Writer {
	private String toTable;
	private ESConnection instance;

	@Override
	public void init() {
		instance = ESConnection.getInstance();
		toTable = target.get(Fields.writeToTable).toString();
		instance.createIndex(toTable);
	}

	@Override
	public void write(List<Map<String, Object>> next) {
		instance.bulkInsert(toTable, next, true);
	}

	@Override
	public void prepareTarget(Map<String, Object> map) {
		Map<String, Object> head = instance.prepareInsert(new HashMap<String, Object>(map));
		instance.prepareIndex(toTable, head);
	}

	@Override
	public void close() throws IOException {
	}
}
