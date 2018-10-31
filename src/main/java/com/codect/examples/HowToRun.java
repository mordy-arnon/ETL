package com.codect.examples;

import java.util.HashMap;
import com.codect.tasks.EtlTask;

public class HowToRun {
	public static void main2(String[] args) {
		System.setProperty("conf.location", "com.codect.conf.FilesConfigurationLoader");
//		new EtlTask("SQL_To_Cloudant", null).run();
//		new EtlTask("SQL_To_cloudant_Customers", null).run();
//		new EtlTask("SQL_To_cloudant_Orders", null).run();
//		new EtlTask("SQL_To_Cloudant_Upsert", null).run();
//		new EtlTask("Cloudant_To_CSV", null).run();
//		new EtlTask("Fox_To_Cloudant", null).run();
		new EtlTask("Aliexpress_To_Cloudent", null).run();
	}
	
	public static void main(String[] args) {
		System.setProperty("conf.location", "com.codect.conf.MongoConfigurationLoader");
		new EtlTask("SqlEventsToMongo", new HashMap<String, Object>()).run();
	}
}
