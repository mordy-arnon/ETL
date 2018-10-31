package com.codect.conf;

import java.util.Map;

public abstract class ConfigurationLoader {

    public static ConfigurationLoader getInstance() throws RuntimeException{
        try{
        String property = System.getProperty("conf.location");
		return (ConfigurationLoader) Class.forName(property).newInstance();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Map<String,Object> readConfFor(String taskName);
    
    public abstract void saveConfFor(String taskName, Map<String,Object> source, Map<String,Object> target);
    
	public abstract Object getGlobalProperty(String key);

	public void saveMax(String string, Map<String, Object> maxFromConfig) {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Object> getMax(String string) {
		// TODO Auto-generated method stub
		return null;
	}
}
