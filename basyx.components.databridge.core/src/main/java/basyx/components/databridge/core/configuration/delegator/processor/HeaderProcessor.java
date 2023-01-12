package basyx.components.databridge.core.configuration.delegator.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basyx.components.databridge.core.configuration.delegator.response.ResponseMessage;

public class HeaderProcessor implements Processor {
	
	@Override
	public void process(Exchange exchange) throws Exception {
		exchange.getIn().removeHeader(Exchange.HTTP_URI);
	}
}
