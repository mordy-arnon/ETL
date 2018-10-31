package com.codect.readers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.codect.common.DBObjectUtil;
import com.codect.common.JsonFieldFixer;
import com.codect.connections.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
/**
 *  Mongo Data converted to query or aggregation (aggr)  
 *  use:   "source" : {
    "class" : "MongoReader",
    "collection" : "mongo_collection1",
    "aggr" : [{
        "#match" : {
          "TAR_IDKUN" : "+params.MAX_TAR_IDKUN_FAC_NDA_CHOVOT"
        }
      }, {
        "#group" : {
          "_id" : "#MIS_KOLEKTIV_MEAGED_BP",
          "IND_KOLEKTIV_HESDER" : {
            "#max" : "#IND_KOLEKTIV_HESDER"
          }
        }
      }]
  },
 *  or a query:   
 *  "source" : {
    "class" : "MongoReader",
    "collection" : "mongo_collection2",
    "filter" : {
      "_id" : 0
    },
    "query" : {
      "MIS_KOLEKTIV_MEAGED_BP" : {
        "#ne" : null
      }
    }
    "sort":...   "limit":...   "batchSize":...
  },
 *  
 *  support:
 *  instead of '$' use '#'
 *  "+params." insert value from parameters.
 *  addToDate(day/month,-3)
 */
public class MongoReader extends Reader implements JsonFieldFixer {
    private Map<String, Object> conf;
    private MongoCursor<Document> fullQuery;
    private int batchSize;

    @Override
    public List<Map<String, Object>> next() {
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < batchSize; i++) {
            ret.add((Map<String, Object>) fullQuery.next());
            if (!fullQuery.hasNext())
                break;
        }
        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void init() {
        this.conf = source;
        try {
            String collection = (String) conf.get("collection");
            List<BasicDBObject> aggr = (List<BasicDBObject>) conf.get("aggr");
            if (conf.get("batchSize")==null)
            	batchSize=1000;
            else
            	batchSize = (int)conf.get("batchSize");
            if (aggr!=null){
                DBObjectUtil.fixJson(aggr,this,"");
                fullQuery=MongoConnection.getInstance().getCol(collection).aggregate(aggr).useCursor(true).allowDiskUse(true).batchSize(batchSize).iterator();
            }else{
                Map query = (Map) conf.get("query");
                query = (query == null) ? new HashMap() : query;
                DBObjectUtil.fixJson(query,this,"");
                Map filter = (Map) conf.get("filter");
                filter = (filter == null) ? new HashMap() : filter;
                Map sort = (Map) conf.get("sort");
                sort = (sort == null) ? new HashMap() : sort;
                Object limit = conf.get("limit");
                FindIterable<Document> result = MongoConnection.getInstance().getCol(collection)
        		.find(new Document(query))
        		.projection(new BasicDBObject(filter))
        		.sort(new BasicDBObject(sort));
                if (limit!=null)
                	result.limit((int) limit);
                fullQuery = result.batchSize(batchSize).noCursorTimeout(true).iterator();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        return fullQuery.hasNext();
    }
    
    @Override
    public void close() throws IOException {
        fullQuery.close();
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

	@Override
	public boolean needToFixValue(String canonicalKey) {
		return true;
	}

	@Override
	public Object fixValue(Object object) {
        if (("" + object).startsWith("toDate")){
            //todate(+params.fromDate+,dd/MM/yyyy)
            String parts[]=("" + object).substring(7, ("" + object).length()-1).split(",");
            try {
                return new SimpleDateFormat(parts[1]).parse(parts[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (("" + object).startsWith("toInt")){
        	String parts=("" + object).substring(6,("" + object).length()-1);
            try {
                return Integer.parseInt(parts);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (("" + object).startsWith("toLong")){
        	String parts=("" + object).substring(7,("" + object).length()-1);
            try {
                return Long.parseLong(parts);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
		if ((""+object).startsWith("#")){
		    return "$"+(""+object).substring(1);
		}
		if ((""+object).startsWith("addToDate")){  // addToDate(day/month,-3)
		    String[] split = (""+object).substring(10,(""+object).length()-1).split(",");
		    Calendar c=Calendar.getInstance();
		    if ("day".equals(split[0]))
		        c.add(Calendar.DAY_OF_MONTH, Integer.parseInt(split[1]));
		    if ("month".equals(split[0]))
		        c.add(Calendar.MONTH, Integer.parseInt(split[1]));
		    return c.getTime();
		}
		if(object instanceof Integer){
		    return ((Number)object).intValue();
		}
		if(object instanceof Long){
		    return ((Long)object).longValue();
		}
		return object;
	}

	@Override
	public boolean needToFixKey(String canonicalKey) {
		return canonicalKey.contains(".#");
	}

	@Override
	public String fixKey(String key) {
		return "$"+key.substring(1);
	}
}
