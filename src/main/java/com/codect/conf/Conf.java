package com.codect.conf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class Conf {

	private Properties p = new Properties();
	private static Conf instance = new Conf();

	private Conf() {
		try {
			p.load(new FileReader(new File("conf.properties")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public static Conf I() {
	// return instance;
	// }
	//
	// public String get(String key) {
	// return p.getProperty(key);
	// }

	public static String get(String key) {
		return instance.p.getProperty(key);
	}

	public static void set(String key, String value) {
		instance.p.setProperty(key,value);
	}
	public static void save() {
		OutputStream output = null;

		try {
			output = new FileOutputStream("conf.properties");
			instance.p.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}