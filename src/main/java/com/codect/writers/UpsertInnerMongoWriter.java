package com.codect.writers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.context.ApplicationContext;
import com.codect.connections.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
/**
 * used to Upsert inner Item in list
 * 
 * @author Mordy.  Linor code + Lael fix for dates
 */
public class UpsertInnerMongoWriter extends InsertMongoWriter {

	private List<String> keys;
	private MongoConnection mc;
	private List<String> innerKeys;
	private String listToPushPull;
	private String upsert;
	private String writeToTable;

	@SuppressWarnings("rawtypes")
	@Override
	public void write(List<Map<String, Object>> list) {
		List<WriteModel<Document>> bulk = new ArrayList<WriteModel<Document>>();
		for (Map<String, Object> record : list) {
			BasicDBObject where = new BasicDBObject();
			for (String key : keys) {
				where.put(key, record.get(key));
				record.remove(key);
			}
			BasicDBObject innerRecordKeys = new BasicDBObject();
			for (String innerKey : innerKeys) {
				innerRecordKeys.put(innerKey, record.get(innerKey));
			}
			String timestampKey = null;
			String timestampValue = null;
			Iterator it = record.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				if (pair.getKey().toString().endsWith("CreatedTimestamp")) {
					timestampKey = pair.getKey().toString();
					timestampValue = pair.getValue().toString();
					break;
				}
			}
			if (timestampKey != null) {
				ArrayList<HashMap<String, Object>> timestampList = new ArrayList<>();
				HashMap<String, Object> firstrule_inner = new HashMap<>();
				HashMap<String, Object> firstrule_outer = new HashMap<>();
				HashMap<String, Object> secondrule_inner = new HashMap<>();
				HashMap<String, Object> secondrule_outer = new HashMap<>();
				firstrule_inner.put("$lt", timestampValue);
				firstrule_outer.put(timestampKey, firstrule_inner);
				secondrule_inner.put("$exists", false);
				secondrule_outer.put(timestampKey, secondrule_inner);
				timestampList.add(firstrule_outer);
				timestampList.add(secondrule_outer);
				where.put("$or", timestampList);
			}
			UpdateOneModel<Document> pull = new UpdateOneModel<>(where,
					new Document("$pull", new Document(listToPushPull, innerRecordKeys)));
			UpdateOneModel<Document> push = new UpdateOneModel<Document>(where,
					new Document("$push", new Document(listToPushPull, record)));
			if (upsert != null) {
				push.getOptions().upsert(true);
			}
			bulk.add(pull);
			bulk.add(push);
		}
		mc.getCollection(writeToTable).bulkWrite(bulk, new BulkWriteOptions().ordered(true));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(ApplicationContext ac) {
		super.init(ac);
		keys = (List<String>) target.get("keys");
		innerKeys = (List<String>) target.get("innerKeys");
		listToPushPull = (String) target.get("listToPushPull");
		upsert = (String) target.get("upsert");
		mc = this.ac.getBean(MongoConnection.class);
		writeToTable = (String) target.get("writeToTable");
	}
}
