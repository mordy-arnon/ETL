package com.codect.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import com.codect.common.Fields;
import com.codect.common.MLog;

public class CsvWriter extends Writer {
	CSVPrinter printer;
	File file;
	FileWriter out;
	BufferedWriter bufferedWriter;
	String path;
	String fileName;
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
	private Set<String> header;

	@Override
	public void close() throws IOException {
		printer.flush();
		if (null != out)
			out.close();
		if (null != bufferedWriter)
			bufferedWriter.close();
		if (null != printer)
			printer.close();
		FileUtils.moveFile(file,new File(path + "/" + fileName + "_" + sdf.format(new Date()) + ".csv"));
	}

	@Override
	public void init() {
		try {
			path = (String) target.get(Fields.path);
			fileName = (String) target.get(Fields.fileName);
			File dir = new File(path+ "/tmp/");
			if (!dir.exists())
				dir.mkdirs();
			file = new File(dir.getAbsolutePath()+"/"+ fileName + "_" + sdf.format(new Date()) + "_tmp.csv");
			if (!file.exists())
				file.createNewFile();
			out = new FileWriter(file, true);
			bufferedWriter = new BufferedWriter(out);
			printer = new CSVPrinter(bufferedWriter, CSVFormat.EXCEL);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(List<Map<String, Object>> next) {
		try {
			for (Map<String, Object> map : next) {
				List<Object> line = new ArrayList<Object>();
				for (String key : header) {
					line.add(map.get(key));
				}
				printer.printRecord(line);
			}
		} catch (IOException e) {
			MLog.error(this, e, "Failed to CSVWriter.write");
		}
	}

	@Override
	public void prepareTarget(Map<String, Object> map) {
		header = map.keySet();
		try {
			printer.printRecord(header);
		} catch (IOException e) {
			MLog.error(this, e, "Failed to CSVWriter.prepareTarget");
		}
	}
}
