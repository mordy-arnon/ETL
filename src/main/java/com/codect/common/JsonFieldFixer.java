package com.codect.common;

public interface JsonFieldFixer {
	public boolean needToFixValue(String canonicalKey);
	public Object fixValue(Object object);
	public boolean needToFixKey(String canonicalKey);
	public String fixKey(String string);
}
