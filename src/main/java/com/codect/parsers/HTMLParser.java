package com.codect.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.codect.common.MLog;

public class HTMLParser extends Parser {

	@Override
	public List<Map<String, Object>> parse(Map<String,Object> source, String html) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try{
		Document doc = Jsoup.parse(html);
		int tableNum = 0;
		try{
			tableNum = Integer.parseInt((String) source.get("tableNumber"));
		}catch(Exception e){}
		String searchBy = source.get("searchBy").toString();
		Elements table = doc.getElementsByAttributeValue(searchBy, 
				source.get("toSearch").toString()).get(tableNum).getAllElements();
		if(table == null){
			MLog.warn(this,null, "No data found for this account : %s", source.get("fullName"));
			return data;
			}
		Element tableHeader;
		if(source.get("HeadersNotFirst")!=null && "yes".equals(source.get("HeadersNotFirst")))
			tableHeader = table.select("tr").get((int)source.get("HeadersLine"));
		else 
			tableHeader = table.select("tr").first();
		ArrayList<String> header = new ArrayList<String>();
		for (Element element : tableHeader.children()) {
			header.add(element.text());
		}
		if(source.get("secondTable")!=null && "yes".equals(source.get("secondTable")))
			table = doc.getElementsByAttributeValue(source.get("searchBy").toString(), (String) source.get("toSearchContent")).get(tableNum).getAllElements();
		Elements trs = table.select("tr");
		for (int i = 1; i < trs.size(); i++) {
			Map<String, Object> row = new HashMap<String, Object>();
			Elements tds = trs.get(i).select("td");
			for (int j = 0; j < tds.size() && j < header.size(); j++) {
				row.put(header.get(j),tds.get(j).text());
			}
			data.add(row);
		}
		return data;
	}
		catch  (Exception e){
			MLog.warn(this, null, "No data found for this account : %s",source.get("fullName"));
			return data;
		}
	}
	
}
