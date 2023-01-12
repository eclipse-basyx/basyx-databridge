package basyx.components.databridge.core.configuration.delegator.response;
import org.apache.camel.Exchange;

public class ResponseMessage {
	private Exchange pahoExchange;

	public Exchange getExchange() {
		return pahoExchange;
	}

	public void setExchange(Exchange exchange) {
		this.pahoExchange = exchange;
	}
	
	public void processMessage(Exchange exchange) {
		System.out.println("Process Message : " + exchange.getIn().getBody(String.class));
		System.out.println("Paho Message : " + getExchange().getIn().getBody(String.class));
	}

}
