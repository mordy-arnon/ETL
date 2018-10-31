package com.codect.writers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.codect.common.Fields;
import com.codect.common.MLog;
import com.codect.connections.MongoConnection;
//import com.fnx.snapshot.dao.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;

public class UpsertMongoWriter extends InsertMongoWriter {
	private List<String> keys;
	private Map<String, Object> fixJoin;

	@Override
	public void close() {		
		List<WriteModel<Document>> bulk = new ArrayList<WriteModel<Document>>();
		if (fixJoin != null) {
			for (String key : fixJoin.keySet()) {
				if (key.equals("deleteMany")) {
					Map<String, Object> value = (Map<String, Object>) fixJoin.get(key);
					Document filter = new Document(value);
					bulk.add(new DeleteManyModel<Document>(filter));
				}
			}
			MongoConnection.getInstance().getCollection(tableName).bulkWrite(bulk);
		}
		super.close();
	}

	@Override
	public void write(List<Map<String, Object>> list) {
		List<WriteModel<Document>> bulk = new ArrayList<WriteModel<Document>>();
		List<String> addToSet = (List<String>) target.get(Fields.addToSet);

		for (Map<String, Object> dbObject : list) {
			BasicDBObject update = new BasicDBObject();
			DBObject fieldsToUpdate = new BasicDBObject();
			if (addToSet != null) {
				for (String item : addToSet) {
					Object obj = dbObject.get(item);
					fieldsToUpdate.put(item, obj);
				}
				update.put("$addToSet", fieldsToUpdate);
			} else
				update = new BasicDBObject("$set", dbObject);

			BasicDBObject updateWhere = new BasicDBObject();
			for (String key : keys) {
				updateWhere.put(key, dbObject.get(key));
			}

			UpdateManyModel<Document> object = new UpdateManyModel<Document>(updateWhere, update);
			if (target.get(Fields.upsert) != null)
				object.getOptions().upsert(true);
			bulk.add(object);
		}
		BulkWriteResult result = MongoConnection.getInstance().getCollection(tableName).bulkWrite(bulk);
		MLog.debug(this, "%s", result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		super.init();
		keys = (List<String>) target.get(Fields.keys);
		fixJoin = (Map<String, Object>) target.get(Fields.fixJoin);
	}
}
