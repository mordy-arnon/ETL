package com.codect.readers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.codect.common.Fields;
import com.codect.conf.Conf;
import com.codect.conf.ConfigurationLoader;

public class SqlIncrementalReader extends SqlReader {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private Map<String,Object> maxFromConfig;
	
	@Override
	public void close() throws IOException {
		super.close();
		ConfigurationLoader.getInstance().saveMax(Conf.get("_id"),maxFromConfig);
	}

	@Override
	public void init() {
		source.put(Fields.query, buildQuery());
		super.init();
	}

	public String buildQuery() {
		String query = (String) source.get(Fields.query);
		maxFromConfig=ConfigurationLoader.getInstance().getMax(Conf.get("_id"));
		for (String key : maxFromConfig.keySet()) {
			Object value = maxFromConfig.get(key);
			if (value instanceof Date)
				value = sdf.format(value);
			query = query.replace("{" + key + "}", "" + value);
		}
		return query;
	}

	@Override
	public List<Map<String, Object>> next() {
		List<Map<String, Object>> next = super.next();
		if (source.get("0rdered")!=null)
			for (Map<String, Object> map : next) {
				max(map);
			}
		else{
			max(next.get(next.size() - 1));
		}
			
		return next;
	}

	public Object max(Map<String, Object> line) {
		String key = maxFromConfig.keySet().iterator().next();
		Object object = line.get(key);
		Object max=maxFromConfig.get(key);
		if (object instanceof Number)
			if (max == null || Integer.valueOf(-1).equals(max) || ((Number) max).intValue() < ((Number) object).intValue())
				max = ((Number) object).intValue();
		if (object instanceof Date) {
			if (max == null || ((Date) max).before((Date) object))
				max = object;
		} else {
			max = object;
		}
		maxFromConfig.put(key, max);
		return max;
	}
}
