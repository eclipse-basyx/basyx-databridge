package basyx.components.databridge.core.configuration.delegator.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basyx.components.databridge.core.configuration.delegator.response.ResponseMessage;

public class RequestProcessor implements Processor {
	private Logger logger = LoggerFactory.getLogger(RequestProcessor.class);
	private ResponseMessage responseMessage;

	public RequestProcessor(ResponseMessage responseMessage) {
		this.responseMessage = responseMessage;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("Message : {}", this.responseMessage.getExchange().getIn().getBody());
		
		Object body = this.responseMessage.getExchange().getIn().getBody();
		exchange.getIn().setBody(body);
		
		logger.info("New Delegator Body : {}", exchange.getIn().getBody());
	}

}
