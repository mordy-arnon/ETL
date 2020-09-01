package com.codect.tasks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.codect.common.DBObjectUtil;
import com.codect.common.MLog;
import com.codect.conf.ConfigurationLoader;
import com.codect.monitoring.MonitorThreads;
import com.codect.readers.Reader;
import com.codect.transformers.Transformer;
import com.codect.writers.Writer;

public class EtlTask implements Runnable {
	private String taskName;
	private Map<String, Object> params;
	private List<Transformer> allTrans=new ArrayList<>();

	public EtlTask(String taskName, Map<String, Object> calculateParameters) {
		this.taskName = taskName;
		this.params = calculateParameters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		MLog.debug(this, "Start %s", taskName);
		MonitorThreads monitorThreads = null;
		try {
			Map<String, Object> conf = ConfigurationLoader.getInstance().readConfFor(taskName);
			monitorThreads = MonitorThreads.createMonitor(conf, taskName);
			if (!monitorThreads.canStart())
				return;
			DBObjectUtil.fixJson(conf, new ConfJsonFieldFixer(params), "");
			conf.put("params", params);
			Map<String, Object> source = (Map<String, Object>) conf.get("source");
			Reader reader = Reader.createReader(source.get("class")).create(conf);
			Map<String, Object> target = (Map<String, Object>) conf.get("target");
			Writer writer = Writer.createWriter(target.get("class")).create(conf);
			reader.init();
			writer.init();
			List<Object> transforms = (List<Object>) conf.get("transforms");
			ThreadPoolExecutor pool = null;
			if (reader.hasNext()) {
				if (!monitorThreads.write())
					return;
				boolean isFirstBulk = true;
				if (conf.get("async") != null) {
					pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(((Number)conf.get("async")).intValue());
				}
				if (transforms != null)
				for (Object transform : transforms) {
					Map<String, Object> transConf = (Map<String, Object>) transform;
					Transformer trans = Transformer.create(transConf.get("class"));
					trans.init(transConf, params);
					allTrans.add(trans);
				}
				do {
					if (!monitorThreads.canContinue())
						break;
					List<Map<String, Object>> next = reader.next();
					for (Transformer trans : allTrans) {
						next = trans.transform(next);
					}
					if (isFirstBulk) {
						isFirstBulk = !isFirstBulk;
						writer.prepareTarget(next.get(0));
					}
					if(next.size()>0){
						monitorThreads.setWrittenRows(next.size());
						write(next, pool, writer);
					}
				} while (reader.hasNext());
			}
			try {
				reader.close();
				writer.close();
				if (pool != null) {
					pool.shutdown();
					pool.awaitTermination(10, TimeUnit.HOURS);
					MLog.debug(this, "loadTask %s: close method - all threads are done", taskName);
				}
				monitorThreads.finish();
			} catch (Exception e) {
				monitorThreads.stop(e);
			}
		} catch (Exception e) {
			monitorThreads.stop(e);
		}
	}

	private void write(final List<Map<String, Object>> next, final ThreadPoolExecutor pool, final Writer writer)
			throws Exception {
		if (pool != null && !pool.isShutdown()) {
			MLog.debug(this, "task %s has %s tasks waiting, after %s that completed", taskName,
					pool.getTaskCount() - pool.getCompletedTaskCount(), pool.getCompletedTaskCount());
			while (pool.getTaskCount() - pool.getCompletedTaskCount() >= pool.getCorePoolSize())
				synchronized (pool) {
					pool.wait();
				}
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						writer.write(next);
					} catch (Exception e) {
						MLog.error(this, e, "loadTask %s:  thread failed", taskName);
					}
					synchronized (pool) {
						pool.notify();
					}
				}
			});
		} else
			writer.write(next);
	}

	public static void main(String[] args) throws InterruptedException {
		int n = 4;
		System.out.println(System.currentTimeMillis());
		final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(n);
		for (int i = 0; i < 13; i++) {
			while (pool.getTaskCount() - pool.getCompletedTaskCount() >= n)
				synchronized (pool) {
					pool.wait();
				}
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (pool) {
						pool.notify();
					}
				}
			});
		}
		pool.shutdown();
		pool.awaitTermination(10, TimeUnit.HOURS);
		System.out.println(System.currentTimeMillis());
	}
}
