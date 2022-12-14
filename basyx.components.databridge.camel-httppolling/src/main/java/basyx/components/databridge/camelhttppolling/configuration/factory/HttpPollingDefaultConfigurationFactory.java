package basyx.components.databridge.camelhttppolling.configuration.factory;

import basyx.components.databridge.camelhttppolling.configuration.HttpPollingConsumerConfiguration;
import basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;

/**
 * A default configuration factory for Http polling from a default file location
 * @author Niklas Mertens
 *
 */
public class HttpPollingDefaultConfigurationFactory extends DataSourceConfigurationFactory {
	public static final String DEFAULT_FILE_PATH = "httpconsumer.json";
	
	public HttpPollingDefaultConfigurationFactory(ClassLoader loader) {
		super(DEFAULT_FILE_PATH, loader, HttpPollingConsumerConfiguration.class);
	}
	
	public HttpPollingDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, HttpPollingConsumerConfiguration.class);
	}
}
