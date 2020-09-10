package com.codect.common;
 
import java.util.Map;
import java.util.HashMap;
import org.bson.Document;
import org.springframework.context.ApplicationContext;
import com.codect.connections.MongoConnection;
import com.mongodb.client.FindIterable;

/**
 * basic class to create cache from Mongo Tables.
 * use keys if you have more then one.
 * use values - if document catch too much memory.
 * call clearAndReload to refresh cache.
 *
 * @author Mordy. source from Lael + small changes.
 */
public class OdsCache {
    private ApplicationContext ac;
    private HashMap<String, Object> mydata;
    private String[] keys;
    private String[] values;
    
    public OdsCache(ApplicationContext ac, String collection, String[] keys,String[] values) {
        this.ac = ac;
        this.keys=keys;
        this.values=values;
        clearAndReload();
    }
    
    public void clearAndReload(){
        FindIterable<Document> myCollectionData = this.ac.getBean(MongoConnection.class).getCollection(collection).find(new Document());
        Map<String,Object> mydata = new HashMap<>();
        for (Document document : myCollectionData) {
            String mykey = "";
            for(int i=0;i<keys.length;i++){
                mykey+= (String) document.get(keys[i]);
            }
            Map<String,Object> myValue=document; 
            if (values!=null){
                myValue=new HashMap();
                for(int i=0;i<values.length;i++){
                     myValue.put(values[i],document.get(values[i]));
                }
            }
            mydata.put(mykey, myValue);
        }
        this.mydata=mydata;
    }

    public Document getRecordById(String ifyun_sch) {
        return (Document) mydata.get(ifyun_sch);
    }
}
