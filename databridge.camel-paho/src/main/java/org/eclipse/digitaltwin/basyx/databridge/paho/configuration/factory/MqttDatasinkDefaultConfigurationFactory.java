package org.eclipse.digitaltwin.basyx.databridge.paho.configuration.factory;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.DataSinkConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.paho.configuration.MqttDatasinkConfiguration;

public class MqttDatasinkDefaultConfigurationFactory extends DataSinkConfigurationFactory{

	private static final String FILE_PATH = "mqtt_datasink.json";

	public MqttDatasinkDefaultConfigurationFactory(ClassLoader loader) {
		super(FILE_PATH, loader, MqttDatasinkConfiguration.class);
		// TODO Auto-generated constructor stub
	}

	public MqttDatasinkDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, MqttDatasinkConfiguration.class);
	}

}
