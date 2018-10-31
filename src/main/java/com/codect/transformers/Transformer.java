package com.codect.transformers;

import java.util.List;
import java.util.Map;

public abstract class Transformer {
	protected Map<String, Object> conf;
	protected Map<String, Object> params;
	
    public static Transformer create(Object transform) {
        try{
            return (Transformer) Class.forName("com.codect.transformers."+transform).newInstance();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract List<Map<String, Object>> transform(List<Map<String, Object>> next);

    public void init(Map<String, Object> transConf, Map<String, Object> params) {
        this.conf=transConf;
        this.params=params;
    }
}
