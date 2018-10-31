package com.codect.readers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.codect.common.MLog;

public class HtmlUnitReader extends Reader {
	WebClient webClient;
	private boolean hasNext = true;

	@Override
	public boolean hasNext() {
		boolean t = hasNext;
		hasNext = false;
		return t;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> next() {
		List<Map<String, Object>> steps = (List<Map<String, Object>>) source.get("steps");
		List<String> urls = (List<String>) source.get("urlList");
		if(null == urls || urls.size() == 0)
			urls = getUrls();
		urls=new ArrayList<>(new HashSet<>(urls));
		List<Map<String, Object>> next = new ArrayList<Map<String, Object>>();
		for (String url : urls) {
			HtmlPage page = navigate(steps, url);
			String string = source.get("dataXpath").toString();
			List<DomElement> navigat = (List<DomElement>) page.getByXPath(string);
			for (int i = 0; i < navigat.size(); i++) {
				
				next.add(handleOneElement(navigat.get(i)));
			}
		}
		return next;
	}

	@SuppressWarnings("unchecked")
	private List<String> getUrls() {
		List<String> urls = new ArrayList<String>();
		try {
			Map<String,Object> superPage = (Map<String, Object>) source.get("superPage");
			Map<String, Object> map = new HashMap<String,Object>();
			map.put("method", "getPage");
			HtmlPage page = navigate(map, null, (String) superPage.get("url"));
			List<DomElement> navigat = (List<DomElement>) page.getByXPath("//a");
			Pattern p = Pattern.compile((String) superPage.get("pattern"));
			for (DomElement domElement : navigat) {
				String attribute = domElement.getAttribute("href");
				Matcher m = p.matcher(attribute);
				if(m.lookingAt())
					urls.add(attribute);
			}
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}
		return urls;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> handleOneElement(DomElement htmlElement) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ArrayList<Map<String, Object>> fields = (ArrayList<Map<String, Object>>) source.get("fields");
		for (Map<String, Object> field : fields) {
			String xpathExpr = (String) field.get("Xpath");
			if ("true".equals(field.get("relative"))) {
				String canonicalXPath = htmlElement.getCanonicalXPath();
				xpathExpr = canonicalXPath + xpathExpr;
			}
			List<DomElement> byXPath = (List<DomElement>) htmlElement.getByXPath(xpathExpr);
			if (null != byXPath && byXPath.size() > 0) {
				String attr = (String) field.get("attribute");
				if (null != attr)
					ret.put((String) field.get("name"), getAttribute(byXPath, attr));
				else
					ret.put((String) field.get("name"), getTextContent(byXPath));
			}
		}
		return ret;
	}

	private Object getTextContent(List<DomElement> byXPath) {
		if (byXPath.size() == 1)
			return byXPath.get(0).getTextContent();
		else {
			List<Object> ret = new ArrayList<Object>();
			for (DomElement object : byXPath) {
				ret.add(object.getTextContent());
			}
			return ret;
		}
	}

	private Object getAttribute(List<DomElement> byXPath, String attr) {
		if (byXPath.size() == 1)
			return byXPath.get(0).getAttribute(attr);
		else {
			List<Object> ret = new ArrayList<Object>();
			for (DomElement object : byXPath) {
				ret.add(object.getAttribute(attr));
			}
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	private HtmlPage navigate(List<Map<String, Object>> steps, String url) {
		HtmlPage page = null;
		for (Map<String, Object> step : steps) {
			try {
				page = navigate(step, page, url);
				Map<String, Object> attr = (Map<String, Object>) step.get("attr");
				if (null != attr)
					for (String elem : attr.keySet()) {
						DomElement domElem = page.getElementByName(elem);
						Map<String, String> values = (Map<String, String>) attr.get(elem);
						for (String attrName : values.keySet()) {
							domElem.setAttribute(attrName, values.get(attrName));
						}
					}
			} catch (FailingHttpStatusCodeException | IOException e) {
				MLog.error(this, e, "error in step list");
			}
		}
		return page;
	}

	private HtmlPage navigate(Map<String, Object> step, HtmlPage prev, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		String method = step.get("method").toString();
		switch (method) {
		case "getPage":
			return getPage(url);
		case "navigetByButtonClick":
			HtmlButton button = (HtmlButton) prev.getByXPath(step.get("xpath").toString()).get(0);
			return button.click();
		}
		return null;
	}
	
	private HtmlPage getPage(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		configureWebClient();
		if (url.startsWith("https")) {
			webClient.getOptions().setUseInsecureSSL(true);
		}
		if(url.startsWith("//"))
			url = "http:"+url;
		PrintStream a=System.out;
		System.setOut(null);
		HtmlPage page2 = webClient.getPage(url);
		System.setOut(a);
		return page2;
	}

	private void configureWebClient() {
		webClient.getOptions().setJavaScriptEnabled("true".equals(source.get("enableJS")));
		if ("NicelyResynchronizing".equals(source.get("AjaxController")))
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		webClient.getOptions().setThrowExceptionOnScriptError("true".equals(source.get("ThrowExceptionOnScriptError")));
		webClient.getOptions().setCssEnabled("true".equals(source.get("CssEnabled")));
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getCookieManager().setCookiesEnabled(true);
	}

	@Override
	public void close() throws IOException {
	}
	
	@Override
	public void init() {
		BrowserVersion browser = getBrowser(source.get("browserVersion").toString());
		if (null != browser)
			webClient = new WebClient(browser);
		else
			webClient = new WebClient();
	}

	private BrowserVersion getBrowser(String name) {
		if ("chrome".equalsIgnoreCase(name))
			return BrowserVersion.CHROME;
		if ("FF38".equalsIgnoreCase(name))
			return BrowserVersion.FIREFOX_38;
		if ("FF45".equalsIgnoreCase(name))
			return BrowserVersion.FIREFOX_45;
		if ("IE".equalsIgnoreCase(name))
			return BrowserVersion.INTERNET_EXPLORER;
		return null;
	}

	@Override
	public void remove() {
	}

 	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = new WebClient();
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getCookieManager().setCookiesEnabled(true);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		HtmlPage page = webClient.getPage("https://www.fox.co.il/en/KIDS/139281/139448");
		List<?> byXPath = page.getByXPath("//*[@id=\"page-main\"]/div[2]/div[1]/nav/ul/li");
		JSONObject obj = new JSONObject();
		obj.put("stam", "//*[@id=\"page-main\"]/div[2]/div[1]/nav/ul/li");
		try {
			FileWriter file = new FileWriter("/Tehila/codectBi_ws/Connector/etls/test.json");
			file.write(obj.toJSONString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
		Pattern p = Pattern.compile("//www.aliexpress.com/item/");
		Matcher m = p.matcher("//www.aliexpress.com/ite/Women-Sexy-Striped-Bodycon-Casual-Cocktail-Party-Mini-Dress-Summer-Dresses-2015/32426230584.html?ws_ab_test=searchweb201556_0,searchweb201602_2_10017_10021_507_10022_10020_10009_10008_10018_10019,searchweb201603_2&btsid=b8e1f2f7-aee7-4858-a88d-78cfe3cf994c");
		System.out.println(m.lookingAt());
	}
}
