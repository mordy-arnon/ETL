package com.codect.parsers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParser extends Parser {

	public static Map<String,Object> getRow(Document document, Element parent) {
		Map<String,Object> headersArray = new HashMap<String, Object>();
	if (parent == null) {
		return headersArray;
	}
	NodeList nl = parent.getChildNodes();
	ArrayList<Element> al = new ArrayList<Element>();
	for (int i = 0; i < nl.getLength(); i++) {
		Node n = nl.item(i);
		if (n instanceof Element) {
			al.add((Element) nl.item(i));
			}
		}
		Iterator<Element> it = al.iterator();
		while (it.hasNext()) {
			Element next = it.next();
			headersArray.put(next.getNodeName(), StringUtils.strip(next.getTextContent()));
			}
	return headersArray;
	}
	
	
	@Override
	public List<Map<String, Object>> parse(Map<String, Object> source, String xmlString) {
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc=null;
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputStream xmlInputStream = new ByteArrayInputStream(xmlString.getBytes());
			doc = dBuilder.parse(xmlInputStream);
		} 
		catch (Exception e){	
		}
				
		doc.getDocumentElement().normalize();
		int countOfItems = StringUtils.countMatches(xmlString, (String) source.get("dataTag"));
	
		for(int i=0; i < countOfItems; i++){
			Map<String, Object> row = new HashMap<String, Object>();
			NodeList nList = doc.getElementsByTagName("data_" + Integer.toString(i));
			Node nNode = nList.item(0);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element eElement = (Element) nNode;
				row = getRow(doc,  eElement);
			}
			data.add(row);
		}
		return data;
	}
}
