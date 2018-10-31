package com.codect.parsers;

import java.util.List;
import java.util.Map;

public abstract class Parser {

	public static Parser createParser(String dataFormat) {
		if ("csv".equals(dataFormat))
			return new CsvParser();
		if ("html".equals(dataFormat))
			return new HTMLParser();
		if ("json".equals(dataFormat))
			return new JSONParser();
		if ("xml".equals(dataFormat))
			return new XmlParser();
		return null;
	}

	public abstract List<Map<String,Object>> parse(Map<String,Object> source, String html);
}
