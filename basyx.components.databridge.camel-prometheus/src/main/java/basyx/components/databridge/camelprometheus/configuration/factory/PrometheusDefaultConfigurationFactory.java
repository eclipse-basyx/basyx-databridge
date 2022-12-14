package basyx.components.databridge.camelprometheus.configuration.factory;

import basyx.components.databridge.camelprometheus.configuration.PrometheusConsumerConfiguration;
import basyx.components.databridge.core.configuration.factory.DataSinkConfigurationFactory;

/**
 * A default configuration factory for polling Prometheus data from a default file location
 * @author n14s - Niklas Mertens
 *
 */
public class PrometheusDefaultConfigurationFactory extends DataSinkConfigurationFactory {
	public static final String DEFAULT_FILE_PATH = "prometheus.json";
	
	public PrometheusDefaultConfigurationFactory(ClassLoader loader) {
		super(DEFAULT_FILE_PATH, loader, PrometheusConsumerConfiguration.class);
	}
	
	public PrometheusDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, PrometheusConsumerConfiguration.class);
	}
}
