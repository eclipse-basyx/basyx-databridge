package basyx.components.databridge.core.configuration.route.request;

import org.apache.camel.builder.RouteBuilder;

import basyx.components.databridge.core.configuration.route.core.IRouteCreator;
import basyx.components.databridge.core.configuration.route.core.IRouteCreatorFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;

public class RequestRouteCreatorFactory implements IRouteCreatorFactory {

	@Override
	public IRouteCreator create(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		return new RequestRouteCreator(routeBuilder, routesConfiguration);
	}

}
