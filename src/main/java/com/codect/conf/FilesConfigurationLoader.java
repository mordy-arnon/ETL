package com.codect.conf;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.codect.common.Fields;

public class FilesConfigurationLoader extends ConfigurationLoader {
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> readConfFor(String taskName) {
		String path = Conf.get("confFilePath") + taskName + ".json";
		JSONObject parse = null;
		try {
			parse = (JSONObject) new JSONParser().parse(new FileReader(path));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return parse;
	}

	@Override
	public Object getGlobalProperty(String key) {
		return Conf.get(key);
	}

	@Override
	public void saveConfFor(String taskName, Map<String, Object> source, Map<String, Object> target) {
		Map<String, Object> conf = readConfFor(taskName);
		String path = Conf.get("confFilePath") + taskName + ".json";
		conf.put(Fields.source, source);
		conf.put(Fields.target, target);
		JSONObject object = new JSONObject(conf);
		try {
			FileWriter file = new FileWriter(path);
			file.write(object.toJSONString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
