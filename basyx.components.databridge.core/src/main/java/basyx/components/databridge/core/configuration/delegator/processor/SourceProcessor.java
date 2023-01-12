package basyx.components.databridge.core.configuration.delegator.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basyx.components.databridge.core.configuration.delegator.response.ResponseMessage;

public class SourceProcessor implements Processor {
	private Logger logger = LoggerFactory.getLogger(SourceProcessor.class);
	private ResponseMessage responseMessage;

	public SourceProcessor(ResponseMessage responseMessage) {
		this.responseMessage = responseMessage;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		this.responseMessage.setExchange(exchange);
		logger.info("Exchange : {}", this.responseMessage.getExchange().getIn().getBody());
	}
}
