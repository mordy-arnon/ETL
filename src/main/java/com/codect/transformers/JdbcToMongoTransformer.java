package com.codect.transformers;

import java.util.Map;
import java.util.Set;

public class JdbcToMongoTransformer extends SingleLineTrans {

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		try {
			Set<String> keySet = line.keySet();
			for (String string : keySet) {
				Object object = line.get(string);
				if (object instanceof Number) {
					if (((Number) object).longValue() == ((Number) object).doubleValue())
						object = ((Number) object).longValue();
					else
						object = ((Number) object).doubleValue();

				} else if (object instanceof String) {
					if (object != null) {
						String data = object.toString();
						String s = "";

						if (null == conf.get("encoding") || "".equals(conf.get("encoding"))) {
							s = data;
						} else {
							s = new String(data.getBytes(), "" + conf.get("encoding"));
						}
						object = s.trim();
					}
				}
				line.put(string, object);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return line;
	}
}
