package com.codect.tasks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.codect.conf.ConfigurationLoader;

/**
 * use to run multi-tasks one after the other, or in Threads. use: {
 * tasks:["etl1TaskName","etl2TaskName","etl3TaskName",
 * {"parallel":["etl4TaskName","etl5TaskName"]},"LastEtlTaskName"] }
 *
 */
public class MultiTask implements Runnable {
	private String taskName;
	private Map<String, Object> params;

	public MultiTask(String taskName, Map<String, Object> calculateParameters) {
		this.taskName = taskName;
		this.params = calculateParameters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {			         
		Map<String, Object> conf = ConfigurationLoader.getInstance().readConfFor(taskName);
		List<Object> tasks = (List<Object>) conf.get("tasks");
		for (Object task : tasks) {
			runGroup(task);
		}
	}

	@SuppressWarnings("unchecked")
	private void runGroup(Object task) {
		if (task instanceof String)
			new EtlTask((String) task, params).run();
		else {
			Map<String, Object> inner = (Map<String, Object>) task;
			if (inner.get("parallel") != null) {
				try {
					List<Object> parallel = (List<Object>) inner.get("parallel");
					Object not = inner.get("numberOfThreads");
					int numOT = 4;
					if (not != null)
						numOT = Integer.parseInt("" + not);
					ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(numOT);
					for (final Object ser : parallel) {
						newFixedThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								runGroup(ser);	
							}
						});	
					}
					newFixedThreadPool.shutdown();
					newFixedThreadPool.awaitTermination(100, TimeUnit.HOURS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				List<Object> serial = (List<Object>) inner.get("serial");
				for (Object ser : serial) {
					runGroup(ser);
				}
			}
		}
	}
}