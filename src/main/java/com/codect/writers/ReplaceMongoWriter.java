package com.codect.writers;

import java.util.ArrayList;
import java.util.Map;
import org.bson.Document;
import com.codect.conf.Conf;
import com.codect.connections.MongoConnection;
import com.mongodb.MongoNamespace;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.RenameCollectionOptions;

public class ReplaceMongoWriter extends InsertMongoWriter {
	private MongoConnection mc = MongoConnection.getInstance();
	private String realTableName;

	@Override
	public void init() {
		realTableName = (String) target.get("writeToTable");
		tableName = "stg_" + realTableName;
	}

	@Override
	public void prepareTarget(Map<String, Object> map) {
		super.prepareTarget(map);
		beginLoadTransaction();
	}

	private void beginLoadTransaction() {
		MongoCollection<Document> col = mc.getCol(tableName);
		col.drop();
		ListIndexesIterable<Document> listindexInfo = mc.getCol(realTableName).listIndexes();
		for (Document index : listindexInfo) {
			Document dbObject = (Document) index.get("key");
			if (dbObject.get("_id") == null) {
				col.createIndex(dbObject);
			}
		}
	}

	@Override
	public void close() {
		MongoCollection<Document> col = mc.getCol(realTableName);
		MongoCollection<Document> stgCol = mc.getCol(tableName);
		ArrayList<Object> into = MongoConnection.getInstance().getDb().listCollectionNames().into(new ArrayList<>());
		if (into.contains(tableName) && stgCol.count()>0) {
			if (into.contains(realTableName)) {
				col.renameCollection(new MongoNamespace(Conf.get("mongo.db.name"), "tmp_"+realTableName),new RenameCollectionOptions().dropTarget(true));
			}
			stgCol.renameCollection(new MongoNamespace(Conf.get("mongo.db.name"), realTableName));
			mc.getCol("tmp_"+realTableName).drop();
		}
	}
}
