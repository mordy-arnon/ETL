package com.codect.tasks;

import java.util.Map;

import com.codect.common.DBObjectUtil;
import com.codect.common.JsonFieldFixer;
import com.codect.conf.ConfigurationLoader;

public class ConfJsonFieldFixer implements JsonFieldFixer {
	private Map<String, Object> params;

	public ConfJsonFieldFixer(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	public boolean needToFixValue(String canonicalKey) {
		return true;
	}

	@Override
	public boolean needToFixKey(String canonicalKey) {
		return false;
	}

	@Override
	public Object fixValue(Object object) {
		if (object instanceof String) {
		    String string = (String)object;
			String par="+params.";
			if (string.startsWith(par))
				return DBObjectUtil.getInnerField(string.substring(par.length()), params);

			int indexOf = string.indexOf(par);
            while (indexOf>=0){
                int end=string.indexOf("+",indexOf+1);
                String key = string.substring(indexOf+8, end);
                Object globalProperty=DBObjectUtil.getInnerField(key, params);
                if (string.length()==end && indexOf==0)
                    return globalProperty;
                string=string.substring(0, indexOf)+globalProperty+string.substring(end+1);
                indexOf = string.indexOf(par);   
            }

			indexOf = string.indexOf("+config.");
			while (indexOf>=0){
				int end=string.indexOf("+",indexOf+1);
				String key = string.substring(indexOf+8, end);
				Object globalProperty=ConfigurationLoader.getInstance().getGlobalProperty(key);
				if (string.length()==end && indexOf==0)
					return globalProperty;
				string=string.substring(0, indexOf)+globalProperty+string.substring(end+1);
				indexOf = string.indexOf("+config.");	
			}
			return string;
		}
		return object;
	}

	@Override
	public String fixKey(String key) {
		return key;
	}
}
