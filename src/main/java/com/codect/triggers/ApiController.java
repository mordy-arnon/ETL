package com.codect.triggers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codect.common.Fields;
import com.codect.common.MLog;
import com.codect.conf.ConfigurationLoader;
import com.codect.tasks.EtlTask;
import com.codect.tasks.MultiTask;

@Controller
@RequestMapping(value = "/api/*")
public class ApiController {
	@RequestMapping(method = RequestMethod.GET, value = "{api_name}", produces = "application/json;charset=UTF-8")
	public @ResponseBody String api(@PathVariable String api_name,@RequestParam Map<String, Object> params) {
		long start = System.currentTimeMillis();
		Map<String, Object> task = ConfigurationLoader.getInstance().readConfFor(api_name);
		if (task==null){
			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("error", "No Configuration For "+api_name);
			return hashMap.toString();
		}
		if (params==null)
			params=new HashMap<>();
		if (task.get("tasks") != null) {
			new MultiTask(api_name, params).run();
		} else if (task.get("source") != null) {
			new EtlTask(api_name, params).run();
		}
		MLog.info(this, "finish api %s in %d (for params %s)", api_name,(System.currentTimeMillis() - start),params.toString());
		return params.get(Fields.results).toString();
	}
}
