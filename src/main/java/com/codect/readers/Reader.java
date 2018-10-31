package com.codect.readers;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class Reader implements Iterator<List<Map<String,Object>>>,Closeable{
	Map<String, Object> source;
	Map<String, Object> params;
	Map<String, Object> conf;
    public static Reader createReader(Object className) {
        try{
            return (Reader) Class.forName("com.codect.readers."+className).newInstance();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void init();

	@SuppressWarnings("unchecked")
	public Reader create(Map<String, Object> conf) {
		this.conf=conf;
		params=(Map<String, Object>) conf.get("params");
		source = (Map<String, Object>) conf.get("source");
		return this;
	}

}
