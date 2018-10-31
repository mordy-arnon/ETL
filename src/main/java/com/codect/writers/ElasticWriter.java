package com.codect.writers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class ElasticWriter extends Writer{
	private TransportClient client;
	private String indexName;
	private String typeName;
	
	public ElasticWriter(String name) {
		InetAddress address;
		try {
			address = InetAddress.getByName("localhost");
			client = new PreBuiltTransportClient(Settings.EMPTY)
					.addTransportAddress(new InetSocketTransportAddress(address, 9300));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void write(List<Map<String, Object>> list) {
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> map = list.get(i);
			Object key = target.get("indexDate");
			if (key != null) {
				System.out.println("" + map.get(key) + " " + map.get(key).getClass().getName());
				indexName = "" + target.get("indexName") + new SimpleDateFormat("-yyyy.MM.dd").format(map.get(key));
			}
			client.prepareIndex(indexName, typeName).setSource(map).get();
		}
	}

	@Override
	public void init() {
		indexName = "" + target.get("indexName");
		typeName = "" + target.get("typeName");		
	}

	public void close() throws IOException {
		client.close();
	}

	@Override
	public void prepareTarget(Map<String, Object> map) {
	}

}