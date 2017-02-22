package com.simplelwm2m.simplelwm2m;

import java.util.Random;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class SimpleResource extends CoapResource {
    
    private String resourceValue;
	
    /**
     * Build a simple, observable CoapResource with a value that can be readen
     * with GET or written with a POST or PUT
     * @param id
     */
	public SimpleResource(String id) {
		super(id);
		setObservable(true);
	}
	
	public String getResourceValue() {
		return resourceValue;
	}
	
	public void setResourceValue(String value) {
		resourceValue = value;
		changed();
	}
	
	/**
	 * GET the value.
	 */
	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, resourceValue);
	}
	
	/**
	 * Changes the value to that of the request.
	 */
	@Override
	public void handlePUT(CoapExchange exchange) {
		resourceValue = exchange.getRequestText();
		exchange.respond(ResponseCode.CHANGED);
		changed();
	}
	
	/**
	 * Changes the value to that of the request.
	 */
	@Override
	public void handlePOST(CoapExchange exchange) {
		resourceValue = exchange.getRequestText();
		exchange.respond(ResponseCode.CHANGED);
		changed();
	}
	

}
