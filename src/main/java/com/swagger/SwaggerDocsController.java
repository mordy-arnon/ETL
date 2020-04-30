package com.codect.triggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.codect.conf.GenerateSwaggerUI;

@RestController
public class SwaggerDocsController {

  @Autowired
	private ApplicationContext ac;
	
	@RequestMapping("/documentation/v2/api-docs")
	@GetMapping
	public String ui() {
		GenerateSwaggerUI p = new GenerateSwaggerUI();
		String swaggerUi;
		String prefix = "{\"swagger\":\"2.0\",\"info\":{\"description\":\"Api Documentation\",\"version\":\"1.0\",\"title\":\"ODS POC API\",\"contact\":{\"name\":\"Mordy Arnon\",\"email\":\"mordy@codect.co\"},\"license\":{}},\"host\":\"localhost:8080\",\"basePath\":\"/\",\"tags\":[{\"name\":\"api-controller\",\"description\":\"Api Controller\"}],\"paths\":";
//		String responses = "\"200\":{\"description\":\"OK\"}}},\"401\":{\"description\":\"Unauthorized\"},\"403\":{\"description\":\"Forbidden\"},\"404\":{\"description\":\"Not Found\"}";
//		String responses_200 = "{\"description\":\"OK\"}";
//		String responses_401 = "{\"description\":\"Unauthorized\"}";
//		String responses_403 = "{\"description\":\"Forbidden\"}";
//		String responses_404 = "{\"description\":\"Not Found\"}";
		MongoConfigurationLoader MCL = new MongoConfigurationLoader();
		HashMap<String, Map<String, Object>> getAllTask = MCL.getAllConfFor(ac);
		swaggerUi = prefix;
		HashMap<Object, Object> paths = new HashMap<>();
		Iterator it = getAllTask.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Map<String, Object> val = (Map<String, Object>) pair.getValue();
			System.out.println(pair.getKey() + " = " + pair.getValue());
			HashMap<Object, Object> path = new HashMap<>();
			paths.put("/api/" + pair.getKey(), path);
			HashMap<Object, Object> get = new HashMap<>();
//			HashMap<Object, Object> myresponses = new HashMap<>();
			path.put("get", get);
			get.put("summary", pair.getKey());
			get.put("tags", Arrays.asList("api-controller"));
			get.put("operationId", pair.getKey());
			get.put("produces", Arrays.asList("application/json;charset=UTF-8"));
			get.put("responses", val.get("responses"));
			get.put("deprecated", false);
			it.remove();
		}
		JSONObject temp = new JSONObject(paths);
		String temp2 = temp.toString();
		swaggerUi = swaggerUi + temp2 + "}";
		swaggerUi = swaggerUi.replace("\\", "");
		return swaggerUi;
	}
}
