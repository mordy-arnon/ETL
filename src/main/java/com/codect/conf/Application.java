package com.codect.conf;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@SpringBootApplication
@ComponentScan(basePackages = "com.codect")
@EnableWebMvc
@Configuration
public class Application {
	public static void main(String[] args) throws IOException {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	    Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
	    Logger.getLogger("org.apache.http").setLevel(Level.OFF);
	    
	    Logger.getRootLogger().setLevel(Level.INFO);
	    Logger.getRootLogger().setLevel(Level.ERROR);
	    
	    RollingFileAppender rollingFileAppender = new RollingFileAppender(
	    		new PatternLayout("[%d{dd/MM/yy hh:mm:ss:sss z}] %5p %c{2}: %m%n"), "/usr/lib/ETL/newnewETL/log/webpalsETL.log", true);
	    rollingFileAppender.setThreshold(Level.INFO);
	    rollingFileAppender.setMaxFileSize("10MB");
	    rollingFileAppender.setMaxBackupIndex(100);
	    
	    RollingFileAppender rollingFileAppenderError = new RollingFileAppender(
	    		new PatternLayout("[%d{dd/MM/yy hh:mm:ss:sss z}] %5p %c{2}: %m%n"), "/usr/lib/ETL/newnewETL/log/webpalsETL_ERROR.log", true);
	    rollingFileAppenderError.setThreshold(Level.ERROR);
	    rollingFileAppender.setMaxFileSize("10MB");
	    rollingFileAppenderError.setMaxBackupIndex(100);
	    
		System.out.println("Application:"+Arrays.toString(args));
		SpringApplicationBuilder parentBuilder = new SpringApplicationBuilder(Application.class);
		SpringApplicationBuilder properties = parentBuilder.properties("server.port:8081","spring.profiles.active:stage");//"server.port:8080"
		properties.run(args);
		Logger.getRootLogger().addAppender(rollingFileAppender);
		Logger.getRootLogger().addAppender(rollingFileAppenderError);
	}

	@Bean
	public ViewResolver viewResolver(ApplicationContext run) {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("/WEB-INF/pages/");
		viewResolver.setSuffix(".jsp");
		return viewResolver;
	}
	
	@Bean
	public ThreadPoolTaskExecutor taskExecutor(){
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(5);
		taskExecutor.setMaxPoolSize(10);
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		return taskExecutor;
	}
	
}
