package com.codect.writers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.codect.common.Fields;
import com.codect.common.MLog;
import com.codect.connections.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;

public class UpsertMongoWriter extends InsertMongoWriter {
	private List<String> keys;
	private Map<String, Object> fixJoin;
	private List<Map<String, String>> addToSet;
	private List<Map<String, String>> setKeys;
	
	@Override
	public void write(List<Map<String, Object>> list) {
		List<WriteModel<Document>> bulk = new ArrayList<WriteModel<Document>>();

		for (Map<String, Object> dbObject : list) {
			BasicDBObject update = new BasicDBObject();
			DBObject fieldsToUpdate = new BasicDBObject();
			if (addToSet != null) {
				for (Map<String, String> item : addToSet) {
					Object obj = DBObjectUtil.getInnerField(item.get("what"),dbObject);
					String where=fixHashtags(item.get("where"),dbObject);
					fieldsToUpdate.put(where, obj);
				}
				update.put("$addToSet", fieldsToUpdate);
			}
			if (setKeys != null){
				for (Map<String, String> item : setKeys) {
					Object obj = DBObjectUtil.getInnerField(item.get("what"),dbObject);
					String where=fixHashtags(item.get("where"),dbObject);
					fieldsToUpdate.put(where, obj);
				}
				update.put("$set", fieldsToUpdate);
			}
			if (target.get("setUnder")!=null){
				keys.stream.foreach(k->dbObject.remove(k));
				String setUnder=dbObject.remove(target.get("setUnder"));
				update.put("$set", new BasicDBObject(setUnder,dbObject));
			}
			
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
		addToSet = (List<Map<String, String>>) target.get(Fields.addToSet);
		setKeys = (List<Map<String, String>>) target.get("set");	
	}

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

	private String fixHashtags(String input,Map<String, Object> data){
		while (input.contains("#")){
			int startChar=input.indexof("#");
			String firstPart=input.substring(0,startChar);
			int endChar=input.indexof("#",startChar+1);
			String key=input.substring(startChar+1,endChar);
			String lastPart=input.substring(endChar+1);
			String value=DBObjectUtil.getInnerField(key,data);
			input=firstPart+value+lastPart;
		}
		return input;
	}
}
