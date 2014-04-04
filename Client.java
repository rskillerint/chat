
package org.alljoyn.bus.sample.chat;

/**
 *
 * @author Shashank
 */

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignalHandler;
import org.alljoyn.bus.MessageContext;
import org.alljoyn.bus.SignalEmitter;

import java.util.Scanner;
import org.alljoyn.bus.ProxyBusObject;

public class Client implements Runnable {

    static {
        System.loadLibrary("alljoyn_java");
    }

    public static void sendMessage(String s) throws BusException {
        myInterface.Notification(s, nickname);
    }

    ///Start of variable declarations
    //////Variables realted to alljoyn connection establishment
    private static final short CONTACT_PORT = 27;
    private static double key = (Math.random() * 100000);
    static BusAttachment mBus;
    static int mHostSessionId = -1;
    static int mUseSessionId = -1;
    private static final String NAME_PREFIX = "org.alljoyn.bus.samples.chat";
    //////Variables realted to alljoyn connection establishment
    
    static int channel_joined = -1;
    static int channel_detected = -1;
    
    private static String[] channels;               //Array for storing all the visible Alljoyn Channel
    private static int channel_count = 0;
    private static int channel_selected = -1;
    private static double[] keys = new double[100]; //Array for storing all the keys that the device received
    private static int key_count = 0;
    private static String nickname ;                //Device nickname that the user has chosen
    private static String alljoynnick;              //Device nickanem that Alljoyn provides
    
    //We can change to a single variable wait for android implementation to be complete
    private static boolean validate = false;        
    private static boolean validate_copy = false;
    //         
    ///Variables for the various interfaces used for data transfer 
    static ChatInterface myInterface = null;
    static SignalInterface mySignalInterface;
    private static ProxyBusObject mProxyObj;
    private static GroupInterface mGroupInterface;
    static Methodhandler myGroup = new Methodhandler();
    
    ///End of variable Declarations
    
    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    // The signal interface is used to send data using alljoyn's signals
    public static class SignalInterface implements ChatInterface, BusObject {
        //Signal via which all the notifications are to be sent
        public void Notification(String s, String nickname) throws BusException {

        }
        //Signal via which all the nickname of new users are to be sent
        @Override
        public void nickname(String usrname, String all_unique) throws BusException {

        }
        //Signal via which the Service/Channel creator validates a new users nickname
        @Override
        public void validate(boolean val) throws BusException {

        }
        //Signal via which users can send their private keys to other devices, thus enabling them to receive their notifications
        @Override
        public void sendKey(Double a) throws BusException {

        }
    }

    //The signal handler reads the signals sent to the device by other devices
    public static class Signalhandler {

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "Notification")
        public void Notification(String string, String nick) {

            if (validate_copy) {
                final String f = string;
                MessageContext ctx = mBus.getMessageContext();

                String as = nick + " -> " + string;
                new messageThread(as).start();
                
                //For Debugging purpose
                String nickname = ctx.sender;
                nickname = nickname.substring(nickname.length() - 10, nickname.length());
                System.out.println(nickname + ": " + string);
            }

        }

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "nickname")
        public void nickname(String usrname, String all_unique) {
        }

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "validate")
        public void validate(boolean val) {
            validate = val;
        }

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "sendKey")
        public void sendKey(Double a) {
            keys[key_count] = a;
            key_count++;
        }
    }

    //The MethodHandler provides implemention for the GroupInterface which contains declarations for alljoyn methods
    public static class Methodhandler implements GroupInterface, BusObject {
        
        // Here only askKey is implemented as Client does not have the member list
        public synchronized double askKey() {
            return key;
        }

        public synchronized String[] getMem() throws BusException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public synchronized String[] getUni() throws BusException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
    
    //The joinChannel GUI calls this method to specify which of the available channels the user selected
    ///////There should be changes here if the GUI validates the usrs input first before passing it on to the back end
    public static void getChannelName(int i) {
        channel_selected = i;
        System.out.println("the channel id is: " + i);
        if (i == -1) {
            channel_selected = -2;
        }
    }

    //This method joins the channel selected by the user
    ///////There should be changes here if the GUI validates the usrs input first before passing it on to the back end
    public static void joinChannel() {
        SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);

        Mutable.IntegerValue sessionId = new Mutable.IntegerValue();

        mBus.enableConcurrentCallbacks();
        // method required which would
        short contactPort = CONTACT_PORT;
        while (channel_selected == -1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Program interupted");
            }
        }
        if (channel_selected == -2) {
            return;
        }
        
        String name = channels[channel_selected];
        Status status = mBus.joinSession(NAME_PREFIX+"."+name, contactPort, sessionId, sessionOpts, new SessionListener());
        if (status != Status.OK) {
            return;
        }
        System.out.println(String.format("BusAttachement.joinSession successful sessionId = %d", sessionId.value));
        mUseSessionId = sessionId.value;
        SignalEmitter emitter = new SignalEmitter(mySignalInterface, sessionId.value, SignalEmitter.GlobalBroadcast.Off);

        myInterface = emitter.getInterface(ChatInterface.class);
        mProxyObj = mBus.getProxyBusObject(NAME_PREFIX+"."+name, "/chatService", sessionId.value, new Class<?>[]{GroupInterface.class});
        mGroupInterface = mProxyObj.getInterface(GroupInterface.class);

        channel_joined = 2;
    }

    //This method run the code for the client side
    public static void run_client() throws BusException, InterruptedException {
        channels = new String[100];
        channel_count=0;
        channel_selected=-1;
        myInterface=null;
        mGroupInterface=null;
        mySignalInterface = new SignalInterface();
        class MyBusListener extends BusListener {

            public void foundAdvertisedName(String name, short transport, String namePrefix) {
                System.out.println(String.format("BusListener.foundAdvertisedName(%s, %d, %s)", name, transport, namePrefix));
                channels[channel_count] = name.substring(29);
                channel_count++;
                channel_detected = 1;
            }

            public void nameOwnerChanged(String busName, String previousOwner, String newOwner) {
                if ("com.my.well.known.name".equals(busName)) {
                    System.out.println("BusAttachement.nameOwnerChagned(" + busName + ", " + previousOwner + ", " + newOwner);
                }
            }

        }

        mBus = new BusAttachment("org.alljoyn.bus.sample.chat", BusAttachment.RemoteMessage.Receive);

        BusListener listener = new MyBusListener();
        mBus.registerBusListener(listener);

        Status status = mBus.connect();
        if (status != Status.OK) {
            return;
        }
        System.out.println("BusAttachment.connect successful");

        
        status = mBus.registerBusObject(mySignalInterface, "/chatService");
        status = mBus.findAdvertisedName(NAME_PREFIX);
        if (status != Status.OK) {
            return;
        }

        System.out.println("BusAttachment.findAdvertisedName successful " + "com.my.well.known.name");
        Signalhandler mySignalHandlers = new Signalhandler();

        status = mBus.registerSignalHandlers(mySignalHandlers);
        if (status != Status.OK) {
            return;
        }

        System.out.println("BusAttachment.registerSignalHandlers successful");

        Methodhandler mySampleService = new Methodhandler();

        status = mBus.registerBusObject(mySampleService, "/chatService");
        if (status != Status.OK) {
            return;
        }
        System.out.println("Method handler Registered");

        while (channel_detected != 1) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Program interupted");
            }
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Join_Channel(channels).setVisible(true);
            }
        });
        joinChannel();
        if (channel_selected == -2) {
            return;
        }
        while (channel_joined != 2) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Program interupted");
            }
        }

        // Channels Joined
        alljoynnick = mBus.getUniqueName();
        Scanner scanner = new Scanner(System.in);
        while (!validate) {
            System.out.println("Please enter a nick name");
            nickname = scanner.nextLine();
            
            myInterface.nickname(nickname, alljoynnick);
            Thread.sleep(1000);
        }
        validate_copy = true;
        System.out.println("Client running");

        String[] uni_names = mGroupInterface.getUni();
        String[] nick = mGroupInterface.getMem();
        for (int i = 0; i < 10; i++) {
            System.out.println(uni_names[i] + " - " + nick[i]);
        }
        while (true) {
            //myInterface.Chat(s,nickname);
            Thread.sleep(5000);
        }
    }

    public static void main(String[] args) throws BusException, InterruptedException {

    }
}

class messageThread extends Thread {

    final String f;

    public messageThread(String s) {
        f = s;
    }

    public void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatFrame(f).setVisible(true);
            }
        });
    }
}
