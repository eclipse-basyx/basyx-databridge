package org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory;

import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.AASDataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.DataSourceConfigurationFactory;

public class AASDatasourceDefaultConfigurationFactory extends DataSourceConfigurationFactory{
	
	private static final String FILE_PATH = "aasserver_datasource.json";
	
	public AASDatasourceDefaultConfigurationFactory(ClassLoader loader) {
		super(FILE_PATH, loader, AASDataSourceConfiguration.class);
	}
	
	public AASDatasourceDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, AASDataSourceConfiguration.class);
	}
}
