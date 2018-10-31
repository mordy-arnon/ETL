package com.codect.transformers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueDecodeTrans extends Transformer{

    @SuppressWarnings("unchecked")
	@Override
    public List<Map<String, Object>> transform(List<Map<String, Object>> next) {
        String fieldName = (String) conf.get("fieldName");
		List<Object> from= (List<Object>) conf.get("from");
        List<Object> to= (List<Object>) conf.get("to");
        HashMap<Object, Object> values=new HashMap<Object, Object>();
        for (int i = 0; i < from.size(); i++) {
            if (from.get(i) instanceof Integer)
                values.put(((Integer)from.get(i)).longValue(), to.get(i));
            else
                values.put(from.get(i), to.get(i));
        }
        for (Map<String, Object> line : next) {
                Object value=line.get(fieldName);
                value=values.get(value);
                line.put(fieldName, value);
        }
        return next;
    }

}
