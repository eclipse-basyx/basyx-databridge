package org.eclipse.digitaltwin.basyx.databridge.aas;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.support.ScheduledPollConsumer;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.facade.SubmodelElementMapCollectionConverter;
import org.eclipse.basyx.vab.coder.json.serialization.DefaultTypeFactory;
import org.eclipse.basyx.vab.coder.json.serialization.GSONTools;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rana
 *
 */
public class AASConsumer extends ScheduledPollConsumer implements PollingConsumer{
	
	
	private static final Logger logger = LoggerFactory.getLogger(AASConsumer.class);
	private final AASEndpoint endpoint;
	private VABElementProxy proxy;
	
	public AASConsumer(AASEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		// TODO Auto-generated constructor stub
		
		//Start connection according to aasserver_datasource.json  
		pollingDataFromDataSource();
	}
	
	@Override
	public AASEndpoint getEndpoint() {
		
		return (AASEndpoint) super.getEndpoint();
	}

	/**
	 * Connect to AAS data source
	 * Three types of scenario
	 * With SubmodelElement, only SubModel or EndPoint
	 */
	public void pollingDataFromDataSource() {
		logger.info("Connecting to SubmodelElement @ " + getEndpoint().getFullProxyUrlAas());
		connectToSmElement();
		return;
	}

	@Override
	protected int poll() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Exchange receive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exchange receiveNoWait() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Polling data and sending to data sink
	 */
	@Override
	public Exchange receive(long timeout) {
		// TODO Auto-generated method stub

		// Polling data periodically
		pollingDataFromDataSource();
		
		// Exchange formated data 
		Exchange exchange = createExchange(getConnectedPropertyToJSON());
		AsyncCallback cb = defaultConsumerCallback(exchange, true);
		getAsyncProcessor().process(exchange, cb);
		
		return exchange;
	}
	
	
	/**
	 * Polling data from AAS Source with SubmodelElement and endpoint
	 */
	private void connectToSmElement() {

		HTTPConnectorFactory factory = new HTTPConnectorFactory();
    	String proxyUrl = this.endpoint.getFullProxyUrlAas();
    	IModelProvider provider = factory.getConnector(proxyUrl);
    	this.proxy = new VABElementProxy("", provider);
	}
	
	public Exchange createExchange(String exchangeProperty) {
		
		Exchange exchange = createExchange(true);
		DefaultMessage exMsg = new DefaultMessage(exchange.getContext());
		exMsg.setBody(exchangeProperty);
		exchange.setIn(exMsg);
		return exchange;
	}
	
	/**
	 * Serialize properties
	 * With specific property name, properties will hold ConnectedProperty
	 * Without specific property , whole submodelElements will hold ConnectedSubmodel
	 */
	private String getConnectedPropertyToJSON() {
		
		if (!getEndpoint().getSubmodelElementIdShortPath().isEmpty()) {
			ConnectedProperty prop = new ConnectedProperty(getProxy());
			return new GSONTools(new DefaultTypeFactory()).serialize(prop.getLocalCopy());
    	}else {
    		
    		ConnectedSubmodel sm = new ConnectedSubmodel(getProxy());
    		return new GSONTools(new DefaultTypeFactory()).serialize(SubmodelElementMapCollectionConverter.smToMap(sm.getLocalCopy()));
    	}
	}
	
	/**
	 * getting proxy
	 */
	VABElementProxy getProxy() {
		return proxy;
	}
	
}
