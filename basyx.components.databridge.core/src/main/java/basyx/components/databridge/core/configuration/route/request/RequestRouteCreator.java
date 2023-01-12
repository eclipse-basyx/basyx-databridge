package basyx.components.databridge.core.configuration.route.request;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.RandomUtils;

import basyx.components.databridge.core.configuration.delegator.processor.HeaderProcessor;
import basyx.components.databridge.core.configuration.delegator.processor.RequestProcessor;
import basyx.components.databridge.core.configuration.delegator.processor.SourceProcessor;
import basyx.components.databridge.core.configuration.delegator.response.ResponseMessage;
import basyx.components.databridge.core.configuration.route.core.AbstractRouteCreator;
import basyx.components.databridge.core.configuration.route.core.RouteConfiguration;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;

public class RequestRouteCreator extends AbstractRouteCreator {

	public RequestRouteCreator(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		super(routeBuilder, routesConfiguration);
	}

	@Override
	protected void configureRoute(RouteConfiguration routeConfig, String dataSourceEndpoint, String[] dataSinkEndpoints,
			String[] dataTransformerEndpoints, String routeId) {
		String delegatorEndpoint = ((RequestRouteConfiguration) routeConfig).getRequestEndpointURI();

		MyBean myBean = new MyBean();

		RouteDefinition routeDefinition;

		if (isDataSourceAProducer(dataSourceEndpoint)) {
			System.out.println("Producer source");
			routeDefinition = createProducerRoute(dataSourceEndpoint, routeId, delegatorEndpoint);
		} else {
			System.out.println("Consumer source");
			routeDefinition = createConsumerRoute(dataSourceEndpoint, routeId, delegatorEndpoint);
		}

		if (!(dataTransformerEndpoints == null || dataTransformerEndpoints.length == 0)) {
			routeDefinition.to(dataTransformerEndpoints).log("Transfer response : ${body}");
		}

		routeDefinition.setBody().simple("${body}").setHeader(Exchange.HTTP_RESPONSE_CODE).constant(200);
	}

	private RouteDefinition createProducerRoute(String dataSourceEndpoint, String routeId, String delegatorEndpoint) {
		return getRouteBuilder().from(delegatorEndpoint).routeId(routeId).process(new HeaderProcessor())
				.to(dataSourceEndpoint).log("Data from URL : ${body}");
	}

	private RouteDefinition createConsumerRoute(String dataSourceEndpoint, String routeId, String delegatorEndpoint) {
		ResponseMessage responseMessage = new ResponseMessage();

		getRouteBuilder().from(dataSourceEndpoint).process(new SourceProcessor(responseMessage))
				.routeId("route" + RandomUtils.nextInt()).log("Data from source processor : ${body}");

		return getRouteBuilder().from(delegatorEndpoint).routeId(routeId).process(new RequestProcessor(responseMessage));
	}

	private boolean isDataSourceAProducer(String dataSourceEndpoint) {
		Endpoint endpoint = getRouteBuilder().getCamelContext().getEndpoint(dataSourceEndpoint);

		return endpoint.isLenientProperties();
	}
}
