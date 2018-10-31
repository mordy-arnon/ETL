package com.codect.tasks;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codect.common.MLog;
import com.codect.conf.ConfigurationLoader;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;

@SuppressWarnings("unchecked")
public class WebTask implements Runnable {
	WebClient webClient;
	private String taskName;
	private Map<String, Object> params;
	private Map<String, Object> result;
	Map<String, Object> source;

	public WebTask(String taskName, Map<String, Object> calculateParameters) {
		this.taskName = taskName;
		this.params = calculateParameters;
		result = new HashMap<String, Object>();
		Map<String, Object> conf = ConfigurationLoader.getInstance().readConfFor(taskName);
		conf.put("params", params);
		source = (Map<String, Object>) conf.get("source");
		init();
	}

	@Override
	public void run() {
		MLog.info(this, "Start %s", taskName);
		try {
			tasks();
			webClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MLog.info(this, "finish %s", taskName);
	}

	public void init() {
		BrowserVersion browser = getBrowser(source.get("browserVersion").toString());
		if (null != browser)
			webClient = new WebClient(browser);
		else
			webClient = new WebClient();
	}

	private void tasks() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		ArrayList<Map<String, Object>> tasks = (ArrayList<Map<String, Object>>) source.get("tasks");
		List<DomElement> byXPath = null;// new ArrayList<DomElement>();
		String type = "", Xpath = "", value = "", newPage = "", urlPage = "";

		HtmlPage currentTaskPage = getPage((String) source.get("url"));
		try {
			for (Map<String, Object> task : tasks) {

				Xpath = (String) task.get("Xpath");
				if (Xpath != null)
					byXPath = (List<DomElement>) currentTaskPage.getByXPath(Xpath);
				newPage = (String) task.get("newPage");
				type = (String) task.get("type");
				value = (String) task.get("value");
				urlPage = (String) task.get("getPage");
				MLog.info(this, "Start type %s", type);
				switch (type) {
				case "getPage":
					currentTaskPage = getPage(urlPage);
					break;
				case "input":
					if (null != byXPath && byXPath.size() > 0) {
						if (task.get("password") != null) {
							HtmlPasswordInput textField = (HtmlPasswordInput) byXPath.get(0);
							textField.setValueAttribute(value);
						} else {
							HtmlTextInput textField = (HtmlTextInput) byXPath.get(0);
							// HtmlInput textField = (HtmlInput) byXPath.get(0);
							textField.fireEvent(Event.TYPE_CHANGE);
							textField.setAttribute("value", value);
							textField.fireEvent(Event.TYPE_CHANGE);
						}
					}
					break;
				case "submit":
					if (null != byXPath && byXPath.size() > 0) {
						HtmlElement button = (HtmlElement) byXPath.get(0);// (HtmlSubmitInput)byXPath.get(0);
						// if (button.getAttribute("disabled") != null)
						// button.removeAttribute("disabled");
						currentTaskPage = button.click();
					}
					break;
				case "button":
					if (null != byXPath && byXPath.size() > 0) {
						HtmlButton button = (HtmlButton) byXPath.get(0);
						currentTaskPage = button.click();
					}
					break;
				case "tables":
					if (null != byXPath && byXPath.size() > 0) {
						boolean canContinue = true;
						//HtmlTable table = (HtmlTable) byXPath.get(0);
						
						String key = "";
						List<Object> tableContent = null;
						if (task.get("result") != null) {
							key = "".equals((String) task.get("result")) ? "tableContent-" + new Random().nextInt()
									: (String) task.get("result");
							if (result.get(key) == null)
								result.put(key, new ArrayList<Object>());
							tableContent = (List<Object>) result.get(key);
						} else {
							key = "tableContent-" + new Random().nextInt();
							result.put(key, new ArrayList<Object>());
							tableContent = (List<Object>) result.get(key);
						}
						String fulXpath = "";
						ArrayList<Map<String, Object>> fields = (ArrayList<Map<String, Object>>) task.get("fields");
						String commonXpath = (String) task.get("commonXpath");
						// for (final HtmlTableBody body : table.getBodies()) {
						for (int i = 1; canContinue; i++) {// i <
							int index = 0;							// body.getRows().size()
							Map<String, Object> rowContent = new HashMap<String, Object>();
							for (Map<String, Object> field : fields) {
								
								String prefixXpath = (String) field.get("prefixXpath");
								String suffixXpath = (String) field.get("suffixXpath");
								fulXpath = String.format("%s[%d]%s%s", commonXpath, i, prefixXpath, suffixXpath);
								// if (i == 0)
								// fulXpath = String.format("%s%s%s",
								// commonXpath, prefixXpath, suffixXpath);
								// else {
								// fulXpath = String.format("%s[%d]%s%s",
								// commonXpath, i, prefixXpath,
								// suffixXpath);
								// }
								byXPath = (List<DomElement>) currentTaskPage.getByXPath(fulXpath);
								if (null != byXPath && byXPath.size() > 0) {
									String name = (String) field.get("name");
									Object content = "";
									if (rowContent.containsKey(name))
										content = rowContent.get(name) + " ";
									rowContent.put(name, content + "" + getTextContent(byXPath));
								} else {
									index++;
									if (index == fields.size()) {
										canContinue = false;
									}
								}
							}
							tableContent.add(rowContent);
						}
					}
					// }
					break;
				case "link":
					if (null != byXPath && byXPath.size() > 0) {
						HtmlAnchor anchor = (HtmlAnchor) byXPath.get(0);
						if (task.get("result") != null) {
							String key = "".equals((String) task.get("result")) ? "TextContent"
									: (String) task.get("result");
							if (result.get(key) == null)
								result.put(key, new ArrayList<Object>());
							List<Object> list = (List<Object>) result.get(key);
							list.add(anchor.getTextContent());
						}
						if (task.get("click") == null || "true".equals((String) task.get("click"))) {
							PrintStream a = System.out;
							System.setOut(null);
							currentTaskPage = anchor.click();
							System.setOut(a);
						}
					}
					break;
				case "prosses":
					if (null != byXPath && byXPath.size() > 0) {
						DomElement htmlElement = byXPath.get(0);
						List<DomElement> listXPath = (List<DomElement>) htmlElement.getByXPath("//a");
						List<String> pattern = (ArrayList<String>) task.get("pattern");
						List<Pattern> pattrens = new ArrayList<Pattern>();
						for (String patt : pattern) {
							pattrens.add(Pattern.compile(patt));
						}

						String key = "".equals((String) task.get("result")) ? "TextContent"
								: (String) task.get("result");
						if (result.get(key) == null)
							result.put(key, new HashSet<Object>());
						for (DomElement domElement : listXPath) {

							if (domElement instanceof HtmlAnchor) {
								HtmlAnchor anchor = (HtmlAnchor) domElement;
								if (task.get("result") != null) {
									String attribute = anchor.getAttribute("href");
									for (Pattern patt : pattrens) {
										Matcher m = patt.matcher(attribute);
										if (m.lookingAt()) {
											Set<Object> list = (Set<Object>) result.get(key);
											list.add(anchor.getTextContent());
										}
									}

								}
							}
						}
					}
					break;
				case "checkbox":
					if (null != byXPath && byXPath.size() > 0) {
						HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) byXPath.get(0);
						if (!checkbox.isChecked()) {
							currentTaskPage = checkbox.click();
						}
					}
					break;
				case "waitForBJS":
					if (value != null)
						webClient.waitForBackgroundJavaScript(Long.valueOf(value).longValue());
					break;
				case "sleep":
					if (value != null)
						Thread.sleep(Long.valueOf(value).longValue());
					break;
				case "refresh":
					if (value != null)
						currentTaskPage.refresh();
					break;
				default:
					break;
				}
				MLog.info(this, "finish type %s", type);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MLog.error(this, e, "error in step task");
		}
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

	public static List<HtmlAnchor> getLinks(HtmlPage page) {
		ArrayList<HtmlAnchor> l = new ArrayList<HtmlAnchor>();
		for (DomElement e : page.getElementsByTagName("a")) {
			if (e instanceof HtmlAnchor) {
				HtmlAnchor ahref = (HtmlAnchor) e;
				l.add(ahref);
			}
		}
		return l;
	}

	private HtmlPage getPage(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		configureWebClient();
		if (url.startsWith("https")) {
			webClient.getOptions().setUseInsecureSSL(true);
		}
		if (url.startsWith("//"))
			url = "http:" + url;
		PrintStream a = System.out;
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

	public static void main(String[] args) {
		System.setProperty("conf.location", "com.codect.conf.DamoConfigurationLoder");
		System.setProperty("configuration.monitor", "com.codect.monitoring.DummyMonitor");
		System.setProperty("configuration.monitor.name", "monitorTasks");

		long startTime = System.currentTimeMillis();
		// new WebTask("log_in_Task", null).run();
		// new WebTask("get_list_RepoName2", null).run();
		// new WebTask("get_list_amazonServers", null).run();
		new WebTask("get_list_RecentEventsSvn", null).run();
		long finishTime = System.currentTimeMillis() - startTime;
	}

}
