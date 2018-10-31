package com.codect.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

public class DBObjectUtil {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fixJson(List list, JsonFieldFixer fixer, String key) {
		for (int i = 0; i < list.size(); i++) {
			Object one = list.get(i);
			if (one instanceof Map)
				fixJson((Map) one, fixer, key);
			else if (one instanceof List)
				fixJson((List) one, fixer, key);
			else {
				if (fixer.needToFixValue(key)) {
					one = fixer.fixValue(one);
					list.set(i, one);
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fixJson(Map all, JsonFieldFixer fixer, String key) {
		String[] keySet = (String[]) all.keySet().toArray(new String[0]);
		for (int i = 0; i < keySet.length; i++) {

			if (fixer.needToFixKey(key + "." + keySet[i])) {
				Object remove = all.remove(keySet[i]);
				keySet[i] = fixer.fixKey(keySet[i]);
				all.put(keySet[i], remove);
			}
			String canonicalKey = key + "." + keySet[i];
			Object one = all.get(keySet[i]);
			if (one instanceof Map)
				fixJson((Map) one, fixer, canonicalKey);
			else if (one instanceof List)
				fixJson((List) one, fixer, canonicalKey);
			else {
				if (fixer.needToFixValue(canonicalKey)) {
					all.put(keySet[i], fixer.fixValue(one));
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	public static Object getInnerField(String dotSeperetedField, Object data) {
		String[] split = dotSeperetedField.split("[.]");
		for (int i = 0; i < split.length; i++) {
			if (data == null || !(data instanceof Map<?, ?>))
				return null;
			data = ((Map<String, Object>) data).get(split[i]);
		}
		return data;
	}

	/**
	 * delete a field from a JSON/Documents
	 * 
	 * @param line
	 * @param fromField
	 * @param clean
	 *            - to clean empty path?
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void deleteByCursor(Map<String, Object> line, String fromField, boolean clean) {
		final String[] fieldParts = fromField.split("[.]");
		Object vals[] = new Object[fieldParts.length];
		int i = 0;
		vals[0] = line;
		while (vals[i] != null && i < fieldParts.length - 1) {
			if (vals[i] instanceof Map)
				vals[i + 1] = ((Map<String, Object>) vals[i]).get(fieldParts[i]);
			else if (vals[i] instanceof List)
				vals[i + 1] = ((List) vals[i]).get(Integer.parseInt(fieldParts[i]));
			else
				break;
			i++;
		}

		Object val = vals[fieldParts.length - 1];
		if (val instanceof Map)
			((Map<String, Object>) val).remove(fieldParts[fieldParts.length - 1]);
		else if (val instanceof List)
			((List) val).remove(Integer.parseInt(fieldParts[fieldParts.length - 1]));
		if (clean) {
			for (int j = vals.length - 1; j > 0; j--) {
				val = vals[j];
				if (val instanceof Map && ((Map<String, Object>) val).keySet().size() == 0
						|| val instanceof List && ((List) val).size() == 0) {
					val = vals[j - 1];
					if (val instanceof Map)
						((Map<String, Object>) val).remove(fieldParts[j - 1]);
					else if (val instanceof List)
						((List) val).remove(Integer.parseInt(fieldParts[j - 1]));
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void recursivePut(Object data, String toField, Object value) {
		String[] split = toField.split("[.]");
		for (int i = 0; i < split.length-1; i++) {
			if (data instanceof Map && ((Map<String, Object>) data).get(split[i])!=null)
				data = ((Map<String, Object>) data).get(split[i]);
			else if (data instanceof List && ((List)data).size()>Integer.parseInt(split[i]))
				data = ((List)data).get(Integer.parseInt(split[i]));
			else{
				try{
					Integer.parseInt(split[i+1]);//check if number
					if (data instanceof Map){
						((Map<String, Object>) data).put(split[i],new ArrayList<>());
						data = ((Map<String, Object>) data).get(split[i]);
					}
					else if (data instanceof List){
						((List) data).add(Integer.parseInt(split[i]),new ArrayList<>());
						data = ((List)data).get(Integer.parseInt(split[i]));
					}
				}catch(NumberFormatException e){
					if (data instanceof Map){
						((Map<String, Object>) data).put(split[i],new HashMap<String,Object>());
						data = ((Map<String, Object>) data).get(split[i]);
					}
					else if (data instanceof List){
						((List) data).add(Integer.parseInt(split[i]),new HashMap<String,Object>());
						data = ((List)data).get(Integer.parseInt(split[i]));
					}
				}
			}
		}
		if (data instanceof Map)
			((Map<String, Object>) data).put(split[split.length-1],value);
		else if (data instanceof List)
			((List) data).add(Integer.parseInt(split[split.length-1]),value);
	}

	public static void main(String[] args) {
		ArrayList<Document> data = new ArrayList<>();
		data.add(new Document("key1", "value"));
		data.add(new Document("key3", "value3"));
		Document document = new Document("key", data).append("key2", "value2");
		deleteByCursor(document, "key.0.key1", true);
		System.out.println(document);
		recursivePut(document, "key5.0.key3", new Date());
		System.out.println(document);
	}
}
