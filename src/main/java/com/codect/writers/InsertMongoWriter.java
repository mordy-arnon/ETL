package com.codect.writers;

 import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.codect.common.Fields;
import com.codect.common.MLog;
import com.codect.connections.MongoConnection;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

public class InsertMongoWriter extends Writer {
    String tableName;

    @Override
    public void write(List<Map<String, Object>> list) {
        List<WriteModel<Document>> batch=new ArrayList<WriteModel<Document>>();
        for (Map<String, Object> dbObject : list) {
            batch.add(new InsertOneModel<Document>(new Document(dbObject)));
        }
        BulkWriteResult result = MongoConnection.getInstance().getCollection(tableName).bulkWrite(batch);
        MLog.debug(this, "%s", result);
    }

    @Override
    public void close() {
    }

    public void init() {
        tableName = (String) target.get("writeToTable");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepareTarget(Map<String, Object> next) {
        List<List<String>> indices =  (List<List<String>>) target.get(Fields.indices);
        if (indices!=null){
            ListIndexesIterable<Document> listIndexes = MongoConnection.getInstance().getCollection(tableName).listIndexes();
            List<Set<String>> existIndices=new ArrayList<Set<String>>();
            for (Document index : listIndexes) {
                Document doc = (Document) index.get("key");
                existIndices.add(doc.keySet());
            }
            for (List<String> list : indices) {
                Document doc = new Document();
                for (String index : list) {
                    doc.put(index, 1);
                }
                if (!contains(existIndices,list))
                    MongoConnection.getInstance().getCollection(tableName).createIndex(doc);
            }
        }
    }

    private boolean contains(List<Set<String>> existIndices, List<String> list) {
        for (Set<String> set : existIndices) {
            if (set.equals(list))
                return true;
        }
        return false;
    }
}
