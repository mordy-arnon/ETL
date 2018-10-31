package com.codect.readers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;

public class SqlReader extends Reader {
	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	boolean next = false;
	private ResultSetMetaData metaData;
	private int batchSize;

	@Override
	public boolean hasNext() {
		return next ;
	}

	@Override
	public List<Map<String, Object>> next() {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		try {
			do {
				HashMap<String, Object> line = new HashMap<String, Object>();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					String columnName = metaData.getColumnName(i);
					Object object = rs.getObject(columnName);
					if (object != null){
						object = prepareValue(object, source);
					}
					line.put(columnName, object);
				}
				ret.add(line);
			} while ((next=rs.next()) && ret.size() < batchSize);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	private int getBatchSize(Map<String, Object> loadTask) {
		try{
		return Integer.parseInt("" + loadTask.get("batchSize"));
		}catch(Exception e){
			return 1000;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void init() {
		try {
			conn = SqlConnection.getInstsance().getConnectionFor(""+source.get("connectionUrl"),""+source.get("driverClass"));
			stmt = conn.createStatement();
			String sql = (String) loadTask.get("query");
			
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				next = true;
				metaData = rs.getMetaData();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Connection getConnection(BasicDBObject basicDBObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	public Object prepareValue(Object object, Map<String, Object> loadTask) throws IOException 
	{
		if (object instanceof Number) 
		{
			if (((Number) object).longValue() == ((Number) object).doubleValue())
				object = ((Number) object).longValue();
			else
				object = ((Number) object).doubleValue();

		}
		else if (object instanceof String) 
		{
			if (object != null) 
			{
				String data = object.toString();
				String s = "";

				if (null ==  loadTask.get("encoding") || "".equals(loadTask.get("encoding")))
				{
					s = data;
				}
				else 
				{
					s = new String(data.getBytes(),""+loadTask.get("encoding"));
				}
				object = s.trim();
			}
		}
		return object;
	}
}
