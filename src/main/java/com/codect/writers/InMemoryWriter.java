package com.codect.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InMemoryWriter extends Writer{
    List<Map<String, Object>> all=new ArrayList<Map<String,Object>>();
    private String key;

    @Override
    public void close() throws IOException {
        key=(String) target.get("key");
        Object dim = target.get("dim");
        if ("0".equals(""+dim))
            params.put(key, all.get(0).get(target.get("fieldName")));
        else
            if ("1".equals(""+dim))
                params.put(key, all.get(0));
        else
            params.put(key, all);
    }

    @Override
    public void write(List<Map<String, Object>> next) {
        all.addAll(next);
    }

    @Override
    public void init() {
    }

    @Override
    public void prepareTarget(Map<String, Object> map) {
    }
}
