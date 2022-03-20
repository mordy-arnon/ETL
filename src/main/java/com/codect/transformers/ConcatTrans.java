package com.codect.transformers;

import java.util.List;
import java.util.Map;

/*
 * add new Field, that is a Concat of String based on DATA, to the Record.
 * config for example:
 * {
 *     "class" : "ConcatTrans",
 *	"list":[{
 *     		"fields" : ["KO_ERETZ","_","MISPAR_ZIHUY","_","BANK"],
 *     		"to":"_id"
 *      },{
 *     		"fields" : ["MCH_OBJETCS.","SNIF","_","MCH","_","SUG_LAK"],
 *     		"to":"innerKey"
 * 	}]
 * }
 */
public class ConcatTrans extends SingleLineTrans {
	@Override
	public Map<String, Object> transLine(Map<String, Object> record) {
		for(Map<String,Object> concConf:((List<Map<String,Object>>)conf.get("list"))){
			List<String> fields = (List<String>) concConf.get("fields");
			String newVal = "";
			for (String col : fields) {
				object data=DBObjectUtil.getInnerField(col,line);
				if (data!=null)
					newVal+=data;
				else 
					newVal+=col;
			}
			DBObjectUtil.recursivePut(record,(String) concConf.get("to"),newVal);
		}
		return line;
	}
}
