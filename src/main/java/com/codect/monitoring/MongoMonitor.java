package com.codect.monitoring;

import java.util.Map;

import com.codect.common.Fields;
import com.codect.connections.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.UpdateOptions;

public class MongoMonitor extends MonitorThreads {

	@Override
	protected Map<String, Object> getLine(String taskName) {
		return MongoConnection.getInstance().getCol(Fields.MonitorThreads)
			.find(new BasicDBObject("_id", taskName)).first();
	}

	@Override
	protected void update(String taskName, Map<String, Object> line) {
		BasicDBObject basicDBObject = new BasicDBObject("$set",line);
		MongoConnection.getInstance().getCol(Fields.MonitorThreads).updateOne(
				new BasicDBObject(Fields._id, taskName),basicDBObject,new UpdateOptions().upsert(true));
	}
}
