package com.codect.transformers;

import java.util.List;
import java.util.Map;

public abstract class SingleLineTrans extends Transformer {

	@Override
	public List<Map<String, Object>> transform(List<Map<String, Object>> next) {
		for (Map<String, Object> line : next) {
			line = transLine(line);
		}
		return next;
	}

	public abstract Map<String, Object> transLine(Map<String, Object> line);
}
