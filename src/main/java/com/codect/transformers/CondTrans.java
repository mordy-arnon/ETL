package com.codect.transformers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.codect.common.CondUtil;

public class CondTrans extends SingleLineTrans {

	@SuppressWarnings("unchecked")
	private void doCond(Map<String, Object> confCond, Map<String, Object> line) {
		Map<String, Object> cond = (Map<String, Object>) confCond.get("cond");
		if(null != cond){
		if (checkCond((Map<String, Object>) cond.get("if"), line, null))
			doThis((Map<String, Object>) cond.get("then"), line);
		else
			doThis((Map<String, Object>) cond.get("else"), line);
		}
	}

	private void doThis(Map<String, Object> map, Map<String, Object> line) {
		if (null == map)
			return;
		for (String key : map.keySet()) {
			if (key.startsWith("line*"))
				line.put(key.substring(key.indexOf('.' + 1)), valueOf(map.get(key), line));
		}
	}

	@SuppressWarnings("unchecked")
	private boolean checkCond(Map<String, Object> map, Map<String, Object> line, Object item) {
		for (String key : map.keySet()) {
			if (map.get(key) instanceof List) {
				if ("in".equals(key))
					return CondUtil.in(item, (Object[]) map.get(key));
				else if("gt".equals(key))
					return CondUtil.gt(item, map.get(key));
				List<Map<String, Object>> conds = (List<Map<String, Object>>) map.get(key);
				List<Boolean> booleans = new ArrayList<Boolean>();
				for (Map<String, Object> cond : conds) {
					booleans.add(checkCond(cond, line, null));
				}
				if ("and".equals(key))
					return CondUtil.and(booleans.toArray(new Boolean[booleans.size()]));
				else if ("or".equals(key))
					return CondUtil.or(booleans.toArray(new Boolean[booleans.size()]));
			}
			else if (key.startsWith("line*")) {
				if (map.get(key) instanceof Map)
					return checkCond((Map<String, Object>) map.get(key), line, line.get(getKeyName(key)));
				return line.get(getKeyName(key)).equals(valueOf(map.get(key), line));
			}
			else if(key.equals("cond"))
				doCond((Map<String, Object>) map.get(key), line);
			else if("not".equals(key))
				return !checkCond((Map<String, Object>) map.get(key), line, item);
		}
		return false;
	}

	private String getKeyName(String key) {
		return key.substring(key.indexOf(".") + 1);
	}

	private Object valueOf(Object value, Map<String, Object> line) {
		if (value.toString().startsWith("line."))
			return line.get(value.toString().substring(value.toString().indexOf('.' + 1)));
		else
			if("now()".equals(value)) return new Date();
			else return value;
	}

	@Override
	public Map<String, Object> transLine(Map<String, Object> line) {
		@SuppressWarnings("unchecked")
		Map<String, Object> cond = (Map<String, Object>) conf.get("cond");
		doCond(cond, line);
		return line;
	}
}
