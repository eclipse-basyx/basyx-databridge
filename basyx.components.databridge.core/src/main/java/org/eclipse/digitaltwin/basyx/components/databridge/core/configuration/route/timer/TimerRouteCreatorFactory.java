package org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.route.timer;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.route.core.IRouteCreator;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.route.core.IRouteCreatorFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;

public class TimerRouteCreatorFactory implements IRouteCreatorFactory {

	@Override
	public IRouteCreator create(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		return new TimerRouteCreator(routeBuilder, routesConfiguration);
	}

}
