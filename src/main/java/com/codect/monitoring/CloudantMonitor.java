package com.codect.monitoring;

import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.codect.common.Fields;
import com.codect.connections.CloudantConnection;

public class CloudantMonitor extends MonitorThreads {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> getLine(String taskName) {
		JSONObject selector = new JSONObject().put("taskName", taskName).put(Fields._id, taskName);
		List<Object> find = CloudantConnection.getInstance().find("mng", selector, null);
		if (null != find && find.size() > 0)
			return (Map<String, Object>) find.get(0);
		return null;
	}

	@Override
	protected void update(String taskName, Map<String, Object> line) {
		Map<String, Object> existline = getLine(taskName);
		if (null == existline) {
			line.put("taskName", taskName);
			line.put(Fields._id, taskName);
			CloudantConnection.getInstance().write("mng", line);
		} else {
			existline.putAll(line);
			CloudantConnection.getInstance().update("mng", existline);
		}
	}

}
