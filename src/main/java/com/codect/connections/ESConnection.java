package com.codect.connections;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.codect.common.Fields;
import com.codect.common.MLog;

public class ESConnection {
	private static DateFormat sdf_for_table = new SimpleDateFormat("yyyy.MM.dd");
	private static ESConnection instance = new ESConnection();
	private Client client;

	public static ESConnection getInstance() {
		return instance;
	}

	public static void setInstance(ESConnection instance) {
		ESConnection.instance = instance;
	}

	private ESConnection() {
		try {
			client = createClient();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Client createClient() throws UnknownHostException {
		NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
		nodeBuilder.settings().put("path.home", "/Tehila/elk/elasticsearch").build();
		NodeBuilder client2 = nodeBuilder.client(true);
		Node node = client2.node();
		client = node.client();
		return client;
	}

	public static String getIndexName(String name) {
		return name + '-' + sdf_for_table.format(new Date());
	}

	public static String getIndexName(String name, Date date) {
		return getIndexName(name);
		// return name + '-' + sdf_for_table.format(date);
	}

	public SearchResponse find(String indexName, int maximumSize, String sortField, RangeQueryBuilder query) {
		String name = getIndexName(indexName);
		SearchResponse allHits = client.prepareSearch(name).setTypes("logs").setQuery(query).addSort("@timestamp", SortOrder.DESC)
				.setSize(maximumSize).get();
		return allHits;
	}

	public SearchResponse findAllAndSort(String indexName, int maximumSize, String sortField) {
		try {
			SearchResponse allHits = client.prepareSearch(indexName).setTypes("logs").addSort(sortField, SortOrder.DESC).setSize(maximumSize).get();
			return allHits;
		} catch (Exception e) {
			System.out.println("exception in findAllAndSort. indexName = " + indexName);
			e.printStackTrace();
			return null;
		}
	}

	public IndexResponse insertRow(String indexName, Map<String, Object> input, Date date) {
		indexName = getIndexName(indexName);
		input = prepareInsert(input);
		IndexResponse actionGet = client.prepareIndex(indexName, "title").setSource(input).get();
		System.out.println("insert id " + actionGet.getId() + " to index " + indexName);
		return actionGet;
	}

	public Map<String, Object> prepareInsert(Map<String, Object> input) {
		Map<String, Object> tmp = new HashMap<String, Object>(input);
		for (String key : tmp.keySet()) {
			Object object = input.get(key);
			if (object instanceof String)
				input.put(key + "-row", object);
		}
		input.put(Fields.lastModified, new Date());
		return input;
	}

	public void setIndexMapping(String indexFullName, XContentBuilder mapping) {
		client.admin().indices().preparePutMapping(indexFullName).setType("logs").setSource(mapping).execute().actionGet();
	}

	public PutMappingResponse prepareIndex(String indexFullName, Map<String, Object> input) {
		// String indexName2 = getIndexName(indexName,
		// (Date)input.get("createDate"));
		XContentBuilder mapping;
		try {
			mapping = XContentFactory.jsonBuilder().startObject().startObject("logs").startObject("properties");
			for (String key : input.keySet()) {
				Object type = input.get(key);
				if (null != type)
					type = type.getClass().getSimpleName().toLowerCase();
				else
					continue;
				if (key.endsWith("-row")) {
					mapping.startObject(key).field("type", type).field("index", "not_analyzed").endObject();
				} else {
					mapping.startObject(key).field("type", type).endObject();
				}
			}
			mapping.endObject().endObject().endObject();
			return client.admin().indices().preparePutMapping(indexFullName).setType("logs").setSource(mapping).execute().actionGet();
		} catch (Exception e) {
			MLog.error(this, e, "error in prepareIndex %s", indexFullName);
		}
		return null;

	}

	public boolean checkIfIndexExists(String index) {

		IndexMetaData indexMetaData = client.admin().cluster().state(Requests.clusterStateRequest()).actionGet().getState().getMetaData()
				.index(index);

		return (indexMetaData != null);

	}

	public IndexResponse insert(String indexName, Map<String, Object> input, Date date) {
		indexName = getIndexName(indexName, date);
		IndexResponse actionGet = client.prepareIndex(indexName, "logs").setSource(input).get();
		return actionGet;
	}

	public IndexResponse insert(String indexFullName, Map<String, Object> input) {
		IndexResponse actionGet = client.prepareIndex(indexFullName, "logs").setSource(input).get();
		return actionGet;
	}

	public IndexResponse dropAllByNamePrefix(String name) throws ExecutionException, InterruptedException {

		Set<String> actionGet = client.admin().indices().stats(new IndicesStatsRequest()).actionGet().getIndices().keySet();
		for (String string : actionGet) {
			if (string.startsWith(name)) {
				dropIndex(string);
			}
		}
		return null;
	}

	public Set<String> getAllIndicesNames() throws ExecutionException, InterruptedException {
		Set<String> keySet = client.admin().indices().stats(new IndicesStatsRequest()).actionGet().getIndices().keySet();
		return keySet;
	}

	public void dropIndex(String indexFullName) {
		System.out.println("drop index " + indexFullName);
		boolean indexExists = client.admin().indices().prepareExists(indexFullName).get().isExists();
		if (indexExists) {
			client.admin().indices().prepareDelete(indexFullName).get();
		}
	}

	public void dropAllIndexes() {
		System.out.println("dont drop all! it will drop kibana dashboard to!");
		// DeleteIndexResponse deleteIndexResponse =
		// client.admin().indices().prepareDelete("_all").get();
	}

	public void createIndex(String indexShortName, Date date) {
		indexShortName = getIndexName(indexShortName, date);
		client.admin().indices().prepareCreate(indexShortName).get();
		boolean indexExists = client.admin().indices().prepareExists(indexShortName).get().isExists();
		if (!indexExists) {
			client.admin().indices().create(new CreateIndexRequest(indexShortName)).actionGet();
			MLog.info(this, "create index %s", indexShortName);
		}
	}

	public void createIndex(String indexFullName) {
		// indexName = getIndexName(indexName);
		boolean indexExists = client.admin().indices().prepareExists(indexFullName).get().isExists();
		if (!indexExists) {
			client.admin().indices().prepareCreate(indexFullName).get();
			MLog.info(this, "create index %s", indexFullName);
		}
	}

	public BulkResponse bulkInsert(String indexFullName, List<Map<String, Object>> results, boolean isFullName) {
		long start = System.currentTimeMillis();
		BulkRequestBuilder requestBuilder = client.prepareBulk();
		for (Map<String, Object> map : results) {

			map = prepareInsert(map);
			IndexRequestBuilder request = client.prepareIndex(indexFullName, "logs").setSource(map);
			requestBuilder.add(request);
		}
		BulkResponse bulkResponse = requestBuilder.execute().actionGet();
		MLog.debug(this, "insert %d rows to %s at %dms", bulkResponse.getItems().length, indexFullName, (System.currentTimeMillis() - start));
		return bulkResponse;
	}

	// public SearchResponse find10KLast(String indexShortName, String fromDate,
	// String sortColumnName) throws ParseException {
	// Date from = Conf.sdf.parse(fromDate);
	// RangeQueryBuilder queryDate =
	// QueryBuilders.rangeQuery(sortColumnName).gt(from);
	// String indexName = getIndexName(indexShortName);
	// SearchResponse allHits =
	// client.prepareSearch(indexName).setTypes("logs").setQuery(queryDate).addSort(sortColumnName,
	// SortOrder.DESC).setSize(10000).get();
	// return allHits;
	// }

	public SearchResponse findLimitedLast(int limit, String indexShortName, Date from, String sortColumnName) throws ParseException {
		RangeQueryBuilder queryDate = QueryBuilders.rangeQuery(sortColumnName).gt(from);
		String indexName = getIndexName(indexShortName);
		SearchResponse allHits = client.prepareSearch(indexName).setTypes("logs").setQuery(queryDate).addSort(sortColumnName, SortOrder.DESC)
				.setSize(limit).get();
		return allHits;
	}
}
