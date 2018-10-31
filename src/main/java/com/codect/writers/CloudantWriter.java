package com.codect.writers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.codect.common.Fields;

public class CloudantWriter extends Writer {
	private CloudantClient client;
	@Override
	public void close() throws IOException {
	}

	@Override
	public void init() {
		client= ClientBuilder.account("codectbi").username("codectbi").password("codectbi").disableSSLAuthentication().build();
	}

	@Override
	public void write(List<Map<String, Object>> next) {
		String value = (String) target.get(Fields.writeToTable);
		for (Map<String, Object> map : next) {
			map.put("TableName", value);
		}
		client.database("fact_db", true).bulk(next).stream().close();
	}

	@Override
	public void prepareTarget(Map<String, Object> map) {
	}
}
