package com.codect.writers;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public abstract class Writer implements Closeable {
	Map<String, Object> target;
	Map<String, Object> params;
	Map<String, Object> conf;
	public static Writer createWriter(Object className) {
		try {
			return (Writer) Class.forName("com.codect.writers." + className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public abstract void write(List<Map<String, Object>> next);

	public abstract void init();

	public abstract void prepareTarget(Map<String, Object> map);

	@SuppressWarnings("unchecked")
	public Writer create(Map<String, Object> conf) {
		this.conf = conf;
		params = (Map<String, Object>) conf.get("params");
		target = (Map<String, Object>) conf.get("target");
		return this;
	}
}
