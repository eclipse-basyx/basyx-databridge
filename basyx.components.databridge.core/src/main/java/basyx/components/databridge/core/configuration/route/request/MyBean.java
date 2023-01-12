package basyx.components.databridge.core.configuration.route.request;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

public class MyBean {
	private Exchange sourceExchange;
	
	@Handler
	public void processRequest(Exchange exchange) {
        // Retrieve the request body as a string
        String requestBody = exchange.getIn().getBody(String.class);

        // Do some processing on the request body
        String processedBody = requestBody.toUpperCase();
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

        // Set the processed body as the response
        exchange.getMessage().setBody(processedBody);
		this.sourceExchange = exchange;
    }
	
	public void processDelegatedRequest(Exchange exchange) {
		if(this.sourceExchange != null) {
			String requestBody = exchange.getIn().getBody(String.class);
			System.out.println("Exchange not null Data is : " + requestBody);
		}
  }
}
