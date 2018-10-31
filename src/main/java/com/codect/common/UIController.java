package com.codect.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codect.readers.Reader;
import com.codect.writers.Writer;

@Controller
@RequestMapping(value = "/ui/*")
public class UIController {
	@RequestMapping(method = RequestMethod.GET, value = "/writer/all", produces = "application/json;charset=UTF-8")
	public @ResponseBody
	String allWriters(Model model) {
		Reflections reflections = new Reflections("com.codect.writers");
		Set<Class<? extends Writer>> subTypes = reflections.getSubTypesOf(Writer.class);
		List<String> writers = new ArrayList<String>();
		for (Class<?> c : subTypes) {
			writers.add(c.getSimpleName());
		}
		return writers.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/writer/{writer_name}", produces = "application/json;charset=UTF-8")
	public @ResponseBody
	String writer(Model model, @PathVariable String writer_name) {
		Writer r = Writer.createWriter(writer_name);
		return r.getClass().getFields().toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/reader/all", produces = "application/json;charset=UTF-8")
	public @ResponseBody
	String allReaders(Model model) {
		Reflections reflections = new Reflections("com.codect.readers");
		Set<Class<? extends Writer>> subTypes = reflections.getSubTypesOf(Writer.class);
		List<String> readers = new ArrayList<String>();
		for (Class<?> c : subTypes) {
			readers.add(c.getSimpleName());
		}
		return readers.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/reader/{reader_name}", produces = "application/json;charset=UTF-8")
	public @ResponseBody
	String reader(Model model, @PathVariable String reader_name) {
		Reader r = Reader.createReader(reader_name);
		return r.getClass().getFields().toString();
	}

	public static void main(String[] args) {
		Reflections reflections = new Reflections("com.codect.writers");
		Set<Class<? extends Writer>> subTypes = reflections.getSubTypesOf(Writer.class);
		List<String> writers = new ArrayList<String>();
		for (Class<?> c : subTypes) {
			writers.add(c.getSimpleName());
		}
	}
}
