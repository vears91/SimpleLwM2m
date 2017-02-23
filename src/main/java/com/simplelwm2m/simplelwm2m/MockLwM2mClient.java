package com.simplelwm2m.simplelwm2m;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.Resource;

public class MockLwM2mClient {
	
    private static final Logger log = Logger.getLogger("MockLwM2mClient");
    
    /**
     * Constants for the encoding values https://www.iana.org/assignments/core-parameters/core-parameters.xhtml
     */
    public static final int TLV_ENCODING = 11542;
    public static final int JSON_ENCODING = 11543;
    public static final int TEXT_ENCODING = 0;
    public static final int OPAQUE_ENCODING = 42;
    public static final int LINK_FORMAT_ENCODING = 40;
        
    /**
     * The LWM2M endpoint for this mock client.
     */
    protected String endpoint;
    
    /**
     * URI for the LWM2M server.
     */
    protected String serverURI; 
  
    /**
     * Encoding to use, defaults to text encoding.
     */
    protected int encoding = TEXT_ENCODING;
    
    /**
     * Binding mode, defaults to UDP.
     */
    protected String bindingMode = "U";
    
    /**
     * Path to target during registration. Defaults to "rd".
     */
    protected String registrationPath = "rd";
    
    /**
     * Duration to use during registration. If 0, the option will not be
     * added to the registration request so the default is used at the 
     * server.
     */
	protected long registrationDuration = 0;
    
	/**
	 * Server for the resources of this mock client
	 */
    protected CoapServer mockClientServer;
    
    /**
     * Californium CoapClient for communication with the LWM2M server.
     */
    protected CoapClient client;
    
    /**
     * Keeps the registration ID after a successful registration.
     */
    protected String registrationId;
    
    /**
     * Socket for the LWM2M server
     */
    protected InetSocketAddress serverAddress;
    
    /**
     * Californium CoapEndpoint for this mock client.
     */
    protected CoapEndpoint coapEndpoint;
    
    /**
     * Holds the LWM2M objects, instances and resources available in this mock device.
     */
    protected ConcurrentHashMap<String, CoapResource> objects;
    
    /**
     * 
     * @return Registration path targeted.
     */
    public String getRegistrationPath() {
		return registrationPath;
	}

    /**
     * Sets the path that will be used by this mock client during registration.
     * @param registrationPath
     */
	public void setRegistrationPath(String registrationPath) {
		this.registrationPath = registrationPath;
	}

	/**
	 * 
	 * @return Returns the set registration duration, 0 if the option
	 * will not be added to registration requests.
	 */
	public long getRegistrationDuration() {
		return registrationDuration;
	}

	/**
	 * Set the registration duration to send in registration requests.
	 * @param registrationDuration
	 */
	public void setRegistrationDuration(long registrationDuration) {
		this.registrationDuration = registrationDuration;
	}
	
	/**
	 * Get the representation of a LWM2M object available in this mock device
	 * by its id. Example: getObject(3303) 
	 * See http://www.openmobilealliance.org/wp/OMNA/LwM2M/LwM2MRegistry.html
	 * @param id
	 * @return
	 */
	public CoapResource getObject(String id) {
    	return objects.get(id);
    }
    
	/**
	 * Adds a {@link CoapResource} as an object. The CoapResource can have other
	 * CoapResource objects as children, to model LWM2M's object/instance/
	 * resource.
	 * Example: To model an instance of  mock temperature sensor that supports
	 * temperature values </3303/0/5700>:
	 * Create a CoapResource with name 5700 (the resource).
	 * Add it to a CoapResource with name 0 (the instance).
	 * Add it to a CoapResource with name 3303.
	 * The CoapResource with name 3303 (the LWM2M object) would be added to
	 * the mock client with this method.
	 * @param id
	 * @param resource
	 */
    public void addObject(String id, CoapResource resource) {
    	this.mockClientServer.add(resource);
    	objects.put(id, resource);
    }
    
    /**
     * Add a {@link SimpleResource} with the specified ID.
     * @param id
     */
    public void addSimpleResource(String id) {
    	SimpleResource r = new SimpleResource(id);
    	mockClientServer.add(r);
    	objects.put(id, r);
    }

	/**
	 * Class constructor
	 * @param endpoint LWM2M endpoint for this mock client
	 * @param serverURI In the format coap://host:port
	 */
    public MockLwM2mClient(String endpoint, String serverURI) {
		NetworkConfig.getStandard();

    	this.endpoint = endpoint;
    	this.serverURI =  serverURI;
    	objects = new ConcurrentHashMap<>();
    	coapEndpoint = new CoapEndpoint(new InetSocketAddress(0));
    	mockClientServer = new CoapServer();
    	registrationId = "";
        
    	InetAddress serverAddr;
		try {
			String[] parts = serverURI.split(":");
        	String host = parts[1].replaceAll("//", "");
        	int port = Integer.parseInt(parts[2]);
			serverAddr = InetAddress.getByName(host);
	    	serverAddress = new InetSocketAddress(serverAddr, port);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Start the Californium endpoint, server and client.
     * This method must be called before communicating with any LWM2M servers.
     * @param register If true, sends a registration request to the server.
     */
    public void start(boolean register) {
       	try {	
        	coapEndpoint.start();
        	mockClientServer.addEndpoint(coapEndpoint);
        	mockClientServer.start();
            client = new CoapClient();
            client.setEndpoint(coapEndpoint);
        	if (register)
        		sendRegistration();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}           
    }
    
    /**
     * Stop the Californium server and endpoint and liberate system resources.
     * @param derregister If true, sends a derregistration request to the
     * server.
     */
    public void stop(boolean derregister) {
    	if (derregister)
    		sendDeregistration();
    	mockClientServer.destroy();
    	coapEndpoint.destroy();
    }
    
    /**
     * Send a derregistration request to the server.
     */
	public void sendDeregistration() {
        Request coapRequest = Request.newDelete();
        coapRequest.setDestination(serverAddress.getAddress());
        coapRequest.setDestinationPort(serverAddress.getPort());
        coapRequest.getOptions().setUriPath(registrationId);
        CoapResponse resp = client.advanced(coapRequest);
	}
	
	/**
	 * Send a registration request to the server.
	 */
    public void sendRegistration() {
    	Request coapRequest = Request.newPost();
    	
        coapRequest.setDestination(serverAddress.getAddress());
        coapRequest.setDestinationPort(serverAddress.getPort());
        coapRequest.getOptions().setContentFormat(LINK_FORMAT_ENCODING);
        coapRequest.getOptions().addUriPath(registrationPath);

        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("ep", endpoint);
        attributes.put("b", bindingMode);
        if (registrationDuration != 0)
        	attributes.put("lt", Long.toString(registrationDuration));
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            coapRequest.getOptions().addUriQuery(attr.getKey() + "=" + attr.getValue());
        }

        coapRequest.setPayload(createObjectRegistrationLinks());

        CoapResponse resp = client.advanced(coapRequest);
        if (resp != null)
        	registrationId = resp.getOptions().getLocationString();
        log.info("Registration: " + registrationId);
    }
    
    private String createObjectRegistrationLinks() {
    	String payload = "";
    	for (Resource objectId : objects.values()) {
    		for (Resource instanceId : objectId.getChildren()) {
    			payload += "</"+objectId.getName()+"/"+instanceId.getName()+">,";
    		}
    	}
    	
    	return payload.substring(0, payload.length()-1);
	}
    
    private String buildPaths(Collection<? extends Resource> elements, String path, String payload) {
    	for (Resource obj : elements) {
    		if (obj.getChildren().size() > 0) {
    			payload += buildPaths(obj.getChildren(), path+obj.getName()+"/", payload);
    		} else {
    			payload += path + obj.getName()+">,";
    		};
    	}
    	return payload;
    }

}