package com.simplelwm2m.simplelwm2m;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class MockLwM2mClientDemo {

    /**
    * Create a mock client that registers to a LWM2M server running on the localhost
    * @param args
    */
    public static void main(String[] args) {
        Random rng = new Random();
        String serverURI = "coap://localhost:5683";
        String endpoint = "mock-";
        String localhostName = "";
        try {
            localhostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long msBetweenNotifyObservers = 1000;
        long msBetweenRegistrations = 100;
        ArrayList<MockLwM2mClient> clients = new ArrayList<>();
        int index = 0;
        int totalClients = 1;
        int totalObservations = 1000;
        /*
        * Parse arguments
        */
        while (index < args.length) {
            String arg = args[index];
            if ("-c".equals(arg)) {
                // Set total number of mock clients to run
                totalClients = Integer.parseInt(args[index+1]);
            } else if ("-s".equals(arg)) {
                //Set server URI
                serverURI = (args[index+1]);
            }  else if ("-nt".equals(arg)) {
                // Set period for observer notification
                msBetweenNotifyObservers = Long.parseLong(args[index+1]);
            }  // Set milliseconds between registrations to avoid overloading the server
            else if ("-rt".equals(arg)) {
                msBetweenRegistrations = Long.parseLong(args[index+1]);
            } else if ("-o".equals(arg)) {
                msBetweenRegistrations = Long.parseLong(args[index+1]);
            }
            else {
                System.err.println("Unknwon arg "+arg);
                System.out.println("Usage:");
                System.out.println("\t-c\tTotal number of clients to run");
                System.out.println("\t-s\tServer URI in the format 'coap://host:port'");
                System.out.println("\t-nt\tMilliseconds between observations");
                System.out.println("\t-rt\tMilliseconds between registrations");
                System.out.println("\t-o\tTotal observations to send");
                return;
            }
            index += 2;
        }

        // Creation of all the clients
        for (int currentClient = 0; currentClient < totalClients; currentClient++) {
            // Add a temperature sensor
            MockLwM2mClient device = new MockLwM2mClient(endpoint+localhostName+"-"+currentClient, serverURI);
            SimpleResource resource = new SimpleResource("5700");
            SimpleResource instance = new SimpleResource("0");
            SimpleResource object = new SimpleResource("3303");
            instance.add(resource);
            object.add(instance);
            device.addObject("3303", object);
            // Start device and send registration
            device.start(true);
            clients.add(device);
            try {
                Thread.currentThread().sleep(msBetweenRegistrations);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int observations = 0; observations < totalObservations; observations++) {
            for (int i = 0; i < clients.size(); i++) {
                /*
                * Change the value of the resource. If an observe relation is established
                * (for instance from the web UI of Leshan's server demo) observers will be
                * notified.
                */
                MockLwM2mClient client = clients.get(i);
                SimpleResource r = (SimpleResource) client.getObject("3303").getChild("0").getChild("5700");
                r.setResourceValue(Integer.toString(rng.nextInt(100)));

            }
            try {
                Thread.currentThread().sleep(msBetweenNotifyObservers);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /*
        * Derregister
        */
        for (int currentClient = 0; currentClient < clients.size(); currentClient++) {
            MockLwM2mClient client = clients.get(currentClient);
            client.stop(true);
            try {
                Thread.currentThread().sleep(msBetweenRegistrations);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }

}
