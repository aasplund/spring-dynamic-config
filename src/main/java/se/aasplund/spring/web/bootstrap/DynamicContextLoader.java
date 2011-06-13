package se.aasplund.spring.web.bootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;

public class DynamicContextLoader extends ContextLoader {

	private static final String DYNAMIC_CONTEXT_CONFIG_LOCATION = "dynamicContextConfigLocation";
	private static final String DEFAULT_DYNAMIC_CONTEXT_CONFIG_LOCATION = "dynamic-spring-config.xml";
	private Set<String> configs = new LinkedHashSet<String>();

	@Override
	protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
		addStaticConfigLocations(applicationContext);
		addDynamicConfigLocations(servletContext);
		mergeConfigLocations(applicationContext);
	}

	private void mergeConfigLocations(ConfigurableWebApplicationContext applicationContext) {
		String[] mergedConfigLocations = new ArrayList<String>(configs).toArray(new String[configs.size()]);
		applicationContext.setConfigLocations(mergedConfigLocations);
	}

	private void addStaticConfigLocations(ConfigurableWebApplicationContext applicationContext) {
		addConfigLocations(applicationContext.getConfigLocations());
	}

	private void addDynamicConfigLocations(ServletContext servletContext) {
		ApplicationContext context = new ClassPathXmlApplicationContext(getDynamicContextConfigLocations(servletContext));
		Collection<ConfigLocationProvider> dynamicConfigLocationProvidors = context.getBeansOfType(ConfigLocationProvider.class).values();
		
		for (ConfigLocationProvider configurator : dynamicConfigLocationProvidors) {
			addConfigLocations(configurator.getConfigLocations());
		}
	}
	
	private void addConfigLocations(String[] configLocations) {
		if (configLocations != null) {
			for (String staticConfig : configLocations) {
				configs.add(staticConfig);
			}
		}
	}

	private String[] getDynamicContextConfigLocations(ServletContext servletContext) {
		String dynamicContextConfigLocation = servletContext.getInitParameter(DYNAMIC_CONTEXT_CONFIG_LOCATION);
		if (dynamicContextConfigLocation == null || dynamicContextConfigLocation == "") {
			dynamicContextConfigLocation = DEFAULT_DYNAMIC_CONTEXT_CONFIG_LOCATION;
		}
		return splitConfigLocation(dynamicContextConfigLocation);
	}

	private String[] splitConfigLocation(String configLocations) {
		String[] parts = configLocations.split("\\,");
		for (int i = 0; i < parts.length; i++)
			parts[i] = parts[i].trim();
		return parts;
	}
}
