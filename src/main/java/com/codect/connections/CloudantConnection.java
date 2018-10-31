package com.codect.connections;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Document;
import com.cloudant.client.api.model.FindByIndexOptions;
import com.cloudant.client.api.model.IndexField;
import com.cloudant.client.api.views.AllDocsRequest;

public class CloudantConnection {
	private CloudantClient client;
	private static CloudantConnection instance = new CloudantConnection();

	public CloudantConnection() {
		client = ClientBuilder.account("codectbi").username("codectbi").password("codectbi").disableSSLAuthentication().build();
	}

	public static CloudantConnection getInstance() {
		return instance;
	}

	public void printAllDbs() {
		List<String> allDbs = client.getAllDbs();
		for (String string : allDbs) {
			System.out.println(string);
		}
	}

	public Database getDB(String dbname) {
		return client.database(dbname, true);
	}

	public List<Document> findAll(String table) throws IOException {
		Database db = getDB(table);
		AllDocsRequest cursor = db.getAllDocsRequestBuilder().build();
		return cursor.getResponse().getDocs();
	}

	public void write(String tableName, Map<String, Object> data) {
		Database db = getDB(tableName);
		db.post(data);
	}

	public List<Object> find(String tableName, JSONObject query, List<String> filter, int limit, IndexField sort) {
		Database db = getDB(tableName);
		FindByIndexOptions findByIndexOptions = new FindByIndexOptions();
		findByIndexOptions.sort(sort);
		findByIndexOptions.limit(limit);
		for (String string : filter) {
			findByIndexOptions.fields(string);
		}
		JSONObject selector = new JSONObject().put("selector", query);
		return db.findByIndex(selector.toString(), Object.class, findByIndexOptions);
	}

	public List<Object> find(String tableName, JSONObject query, List<String> filter) {
		Database db = getDB(tableName);
		FindByIndexOptions findByIndexOptions = new FindByIndexOptions();
		if (null != filter)
			for (String string : filter) {
				findByIndexOptions.fields(string);
			}
		JSONObject selector = new JSONObject().put("selector", query);
		return db.findByIndex(selector.toString(), Object.class, findByIndexOptions);
	}

	public List<Object> find(String tableName, Map<String, Object> query, List<String> filter, int limit, int from) {
		Database db = getDB(tableName);
		FindByIndexOptions findByIndexOptions = new FindByIndexOptions();
		findByIndexOptions.skip(from);
		findByIndexOptions.limit(limit);
		for (String string : filter) {
			findByIndexOptions.fields(string);
		}
		JSONObject selector = new JSONObject().put("selector", new JSONObject(query));
		return db.findByIndex(selector.toString(), Object.class, findByIndexOptions);
	}

	public void write(String tableName, List<Map<String, Object>> data) {
		Database db = getDB(tableName);
		db.bulk(data).stream().close();
	}

	public static void main(String[] args) {
		CloudantConnection.getInstance().printAllDbs();
	}

	public void update(String tableName, List<Map<String, Object>> data) {
		Database db = getDB(tableName);
		db.bulk(data).removeAll(data);
	}
	public void update(String tableName, Map<String, Object> data) {
		Database db = getDB(tableName);
		db.update(data);
	}

}
