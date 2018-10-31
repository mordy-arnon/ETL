package com.codect.common;

import java.util.Date;

public class CondUtil {
	public static boolean in(Object obj, Object... in) {
		for (Object item : in)
			if (obj.equals(item))
				return true;
		return false;
	}

	public static boolean and(Boolean... cond) {
		for (boolean b : cond)
			if (!b)
				return false;
		return true;
	}

	public static boolean or(Boolean... boolean1) {
		for (boolean b : boolean1)
			if (b)
				return true;
		return false;
	}

	public static boolean gt(Object item, Object object) {
		if (and(item instanceof Number, object instanceof Number))
			return ((Number) item).longValue() > ((Number) object).longValue();
		else if (and(item instanceof Date, object instanceof Date))
			return ((Date) item).after((Date) object);
		else if (and(item instanceof String, object instanceof String))
			return ((String) item).compareTo((String) object) > 0;
		return false;
	}
	public static boolean gte(Object item, Object object) {
		if (and(item instanceof Number, object instanceof Number))
			return ((Number) item).longValue() >= ((Number) object).longValue();
		else if (and(item instanceof Date, object instanceof Date))
			return ((Date) item).compareTo((Date) object) >=0;
		else if (and(item instanceof String, object instanceof String))
			return ((String) item).compareTo((String) object) >= 0;
		return false;
	}

	public static boolean lt(Object item, Object object) {
		if (and(item instanceof Number, object instanceof Number))
			return ((Number) item).longValue() < ((Number) object).longValue();
		else if (and(item instanceof Date, object instanceof Date))
			return ((Date) item).before((Date) object);
		else if (and(item instanceof String, object instanceof String))
			return ((String) item).compareTo((String) object) < 0;
		return false;
	}
	public static boolean lte(Object item, Object object) {
		if (and(item instanceof Number, object instanceof Number))
			return ((Number) item).longValue() <= ((Number) object).longValue();
		else if (and(item instanceof Date, object instanceof Date))
			return ((Date) item).compareTo((Date) object) <=0;
		else if (and(item instanceof String, object instanceof String))
			return ((String) item).compareTo((String) object) <= 0;
		return false;
	}
}
