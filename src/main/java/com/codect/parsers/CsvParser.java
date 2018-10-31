package com.codect.parsers;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import com.codect.common.MLog;

public class CsvParser extends Parser {
	@Override
	public List<Map<String, Object>> parse(Map<String,Object> source, String csvString) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		String csvFormat = source.get("csvFormat").toString();
		if(csvString!=null){
		CSVFormat format=CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
		if(csvFormat==null)
			csvFormat="DEFAULT";
		try {
			Field field = CSVFormat.class.getField(csvFormat);
			format = (CSVFormat) field.get(format);
		} catch (Exception e) {
			MLog.error(this, e, "Failed to create format:%s", csvFormat);
		}
		CSVParser parser=null; 
		try {
			parser = new CSVParser(new StringReader(csvString), format);
			Iterator<CSVRecord> iterator = parser.iterator();
			int line=0;
			while(iterator.hasNext()){
				Map<String, String> next = iterator.next().toMap();
				Map<String,Object> next1 = new HashMap<String, Object>(next);
				data.add(next1);
			}
			parser.close();
		} catch (IOException e) {
			e.printStackTrace();
			}
		}
		return data;
	}

}
