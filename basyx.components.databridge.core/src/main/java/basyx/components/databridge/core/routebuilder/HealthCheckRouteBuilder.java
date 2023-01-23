package basyx.components.databridge.core.routebuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheckHelper;
import org.apache.camel.model.dataformat.JsonLibrary;
import basyx.components.databridge.core.dto.HealthCheckResultDTO;

public class HealthCheckRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("jetty:http://localhost:8096/health").id("app.health.context").setHeader(Exchange.CONTENT_TYPE)
				.constant("application/json")
				.process(exchange -> setHealthCheckResult(exchange, HealthCheckHelper.invoke(getContext())))
				.marshal().json(JsonLibrary.Jackson).log("Data : ${body}");
	}

	private void setHealthCheckResult(Exchange exchange, Collection<HealthCheck.Result> healthCheckResults) {
		if (isAllServicesAndRoutesHealthy(healthCheckResults)) { // <1>
			exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 503);
		}

		healthCheckResults.stream().forEach(healthCheck -> System.out.println("Health check: " + healthCheck.getState()));

		List<HealthCheckResultDTO> healthCheckResultDTOs = healthCheckResults.stream().map(HealthCheckResultDTO::toDTO)
				.collect(Collectors.toList());

		exchange.getMessage().setBody(healthCheckResultDTOs);
	}

	private boolean isAllServicesAndRoutesHealthy(Collection<HealthCheck.Result> results) {
		return results.stream().anyMatch(healthCheckResult -> healthCheckResult.getState() != HealthCheck.State.UP);
	}
}
