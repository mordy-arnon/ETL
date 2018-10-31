package com.codect.monitoring;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoClient;

public class DummyMonitor extends MonitorThreads{
	MongoClient mongo=new MongoClient("localhost",27017);	
	
	@Override
	protected Map<String, Object> getLine(String taskName) {
		return new HashMap<>();
	}

	@Override
	protected void update(String taskName, Map<String, Object> line) {		
	}

}
