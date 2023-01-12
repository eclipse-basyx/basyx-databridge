package basyx.components.databridge.core.configuration.factory;

import java.util.List;

import basyx.components.databridge.core.configuration.entity.DelegationConfiguration;

public class DelegationConfigurationFactory extends ConfigurationFactory {

	protected DelegationConfigurationFactory(String filePath, ClassLoader loader, Class<?> mapperClass) {
		super(filePath, loader, mapperClass);
	}
	
	@SuppressWarnings("unchecked")
	public List<DelegationConfiguration> create() {
		return (List<DelegationConfiguration>) getConfigurationLoader().loadListConfiguration();
	}

}
