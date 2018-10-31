package com.codect.monitoring;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.codect.common.MLog;

public abstract class MonitorThreads {
	protected static enum FIELDS {status, exception, startTime, endTime, duration,records};
	protected static enum STATUS {STARTING, WRITING, FINISHED, STOP,CANCELLED};
	private long startTime;
	private int writtenRows=0;
    private String taskName;
    private Map<String, Object> conf;

	public static MonitorThreads createMonitor(Map<String, Object> conf, String taskName) {
		try {
			String className ="com.codect.monitoring.MongoMonitor"; 
			MonitorThreads newInstance = (MonitorThreads) Class.forName(className).newInstance();
			newInstance.init(conf,taskName);
            return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void init(Map<String, Object> conf, String taskName) {
	    this.conf=conf;
        this.taskName=taskName;
    }
	
	protected abstract Map<String, Object> getLine(String taskName);
	protected abstract void update(String taskName, Map<String, Object> line);

	private boolean isRunning() {
        if (!"yes".equals(conf.get("runAlone")))
            return false;
		Map<String, Object> line = getLine(taskName);
		return line != null && line.get(FIELDS.STATUS) != null && !line.get(FIELDS.STATUS).equals(STATUS.STOP.name());
	}

	public boolean canContinue() {
	    if (!"yes".equals(conf.get("runAlone")))
	        return true;

	    Map<String, Object> line = getLine(taskName);
		if (line != null && line.get(FIELDS.STATUS) != null)
			if (STATUS.STOP.name().equals(line.get(FIELDS.STATUS))) {
				MLog.info(this, "task %s: will stop now, as asked.", taskName);
				return false;
			}
		return true;
	}

	public boolean canStart() {
		    if (isRunning())
			return false;
		startTime = System.currentTimeMillis();
		Map<String, Object> line = new HashMap<String, Object>();
		line.put(FIELDS.STATUS.name(), STATUS.INIT.name());
		line.put(FIELDS.START_TIME.name(), new Date());
		update(taskName, line);
		MLog.info(this, "task %s: start running", taskName);
		return true;
	}

	public boolean write() {
	        if (!canContinue())
			return false;
		Map<String, Object> line = new HashMap<String, Object>();
		line.put(FIELDS.STATUS.name(), STATUS.WRITING.name());
		update(taskName, line);
		MLog.info(this, "task %s: start writing", taskName);
		return true;
	}

	public boolean close() {
	        if (!canContinue())
			return false;
		Map<String, Object> line = new HashMap<String, Object>();
		line.put(FIELDS.STATUS.name(), STATUS.CLOSED.name());
		update(taskName, line);
		MLog.info(this, "task %s: start closing", taskName);
		return true;
	}

	public void stop(Exception e) {
		Map<String, Object> line = new HashMap<String, Object>();
		line.put(FIELDS.STATUS.name(), STATUS.STOP.name());
		if (e != null) {
			line.put(FIELDS.EXCEP.name(), toString(e));
		}
		line.put(FIELDS.END_TIME.name(), new Date());
		line.put(FIELDS.DURATION.name(), System.currentTimeMillis() - startTime);
		line.put("numberOfRows", getWrittenRows());		
		update(taskName, line);
		if (e == null)
			MLog.info(this, "task %s: stopped", taskName);
		else
			MLog.error(this, e, "task %s: got error and stopped.", taskName);

		MLog.info(this, "Duration of task  %s is :  %s (ms) ", taskName,
				Long.toString(System.currentTimeMillis() - startTime));
	}

	private Object toString(Exception e) {
		return e.getClass().getName()+ ":"+e.getMessage()+":"+Arrays.toString(e.getStackTrace());
	}

	public int getWrittenRows() {
		return writtenRows;
	}

	public void setWrittenRows(int writtenRows) {
		this.writtenRows += writtenRows;
	}

	public void finish() {
		// TODO Auto-generated method stub
		
	}
}
