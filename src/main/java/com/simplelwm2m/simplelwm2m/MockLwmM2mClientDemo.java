package com.simplelwm2m.simplelwm2m;

import java.util.Random;

public class MockLwmM2mClientDemo {

    /**
    * Create a mock client that registers to a LWM2M server running on the localhost
    * @param args
    */
    public static void main(String[] args) {

        Random rng = new Random();
        String serverURI = "coap://localhost:5683";
        String endpoint = "mockClient";
        MockLwM2mClient device = new MockLwM2mClient(endpoint, serverURI);

        // Add a temperature sensor
        SimpleResource resource = new SimpleResource("5700");
        SimpleResource instance = new SimpleResource("0");
        SimpleResource object = new SimpleResource("3303");
        instance.add(resource);
        object.add(instance);
        device.addObject("3303", object);

        // Start device and send registration
        device.start(true);
        for (int i = 0; i < 60; i++) {
            /*
            * Change the value of the resource. If an observe relation is established
            * (for instance from the web UI of Leshan's server demo) observers will be
            * notified.
            */
            resource.setResourceValue(Integer.toString(rng.nextInt(100)));
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Stop device and derregister
        device.stop(true);
    }

}
