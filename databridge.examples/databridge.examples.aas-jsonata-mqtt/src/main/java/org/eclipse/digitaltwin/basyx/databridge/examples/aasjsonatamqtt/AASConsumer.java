package org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatamqtt;

import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASDatasourceDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.paho.configuration.factory.MqttDatasinkDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;

public class AASConsumer {
	
	private static DataBridgeComponent updater;
	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");
	public static final String AASSERVERPATH = "http://localhost:4001/aasServer";
	

	public static void main(String[] args) {

		
		ClassLoader loader = AASConsumer.class.getClassLoader();
		
		RoutesConfiguration configuration = new RoutesConfiguration();

		// Extend configutation for connections
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		// Extend configuration for Timer
		TimerDefaultConfigurationFactory timerConfigFactory = new TimerDefaultConfigurationFactory(loader);
		configuration.addDatasources(timerConfigFactory.create());
		
		
		// Configures AAS Datasource
		AASDatasourceDefaultConfigurationFactory aasSourceConfigFactory = new AASDatasourceDefaultConfigurationFactory(loader);
		configuration.addDatasources(aasSourceConfigFactory.create());
		
		
		//MQTT as Data Sink
		MqttDatasinkDefaultConfigurationFactory mqttConfigFactory = new MqttDatasinkDefaultConfigurationFactory(loader);
		configuration.addDatasinks(mqttConfigFactory.create());

		
		// Adding Transformer layer
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());
		
		// Start Data Bridge Component
	
		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

}
