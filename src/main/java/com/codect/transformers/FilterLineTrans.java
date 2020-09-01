package com.codect.transformers;

import java.util.List;
import java.util.Map;

public abstract class FilterLineTrans extends Transformer {

	@Override
	public List<Map<String, Object>> transform(List<Map<String, Object>> next) {
		List<Map<String, Object>> result=new ArrayList<>();
    		for (Map<String, Object> line : next) {
			line = transLine(line);
			if (line!=null)
        			result.add(line);
		}
		return result;
	}

	public abstract Map<String, Object> transLine(Map<String, Object> line);
}
