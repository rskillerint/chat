/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alljoyn.bus.sample.chat;

/**
 *
 * @author admin
 */
import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

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
import java.lang.Runnable;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alljoyn.bus.ProxyBusObject;

public class Client implements Runnable {

    static {
        System.loadLibrary("alljoyn_java");
    }

    public static void sendMessage(String s) throws BusException {
        myInterface.Chat(s, nickname);
    }

    private static final short CONTACT_PORT = 27;
    private static double key = (Math.random() * 100000);
    static BusAttachment mBus;
    static int flag = -1;
    static int flag2 = 0;
    static int mHostSessionId = -1;
    static int mUseSessionId = -1;
    static String s = "hello";
    private static final String NAME_PREFIX = "org.alljoyn.bus.samples.chat";

    private static String[] channels;
    private static int channel_count = 0;
    private static int channel_selected = -1;
    private static double[] keys = new double[100];
    private static int key_count = 0;
    private static String nickname = "saurabh";
    private static String alljoynnick;
    private static boolean validate = false;
    private static boolean validate_copy = false;
    static ChatInterface myInterface = null;
    static SampleSignalHandler mySignalInterface;
    private static ProxyBusObject mProxyObj;
    private static GroupInterface mGroupInterface;
    static groupSignalHandler myGroup = new groupSignalHandler();

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class SampleSignalHandler implements ChatInterface, BusObject {

        public void Chat(String s, String nickname) throws BusException {

        }

        @Override
        public void nickname(String usrname, String all_unique) throws BusException {

        }

        @Override
        public void validate(boolean val) throws BusException {

        }

        @Override
        public void sendKey(Double a) throws BusException {

        }
    }

    public static class SignalInterface {

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "Chat")
        public void Chat(String string, String nick) {

            /*
             * See the long comment in doJoinSession() for more explanation of
             * why this is needed.
             * 
             * The only time we allow a signal from the hosted session ID to pass
             * through is if we are in mJoinedToSelf state.  If the source of the
             * signal is us, we also filter out the signal since we are going to
             * locally echo the signal.

             */
            if (validate_copy) {
                final String f = string;
                String uniqueName = mBus.getUniqueName();
                MessageContext ctx = mBus.getMessageContext();

                String nickname = ctx.sender;
                nickname = nickname.substring(nickname.length() - 10, nickname.length());
                System.out.println(nickname + ": " + string);
                String as = nick + " -> " + string;
                new messageThread(as).start();
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

    public static class groupSignalHandler implements GroupInterface, BusObject {

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

    public static void getChannelName(int i) {
        channel_selected = i;
        System.out.println("the channel id is: " + i);
        if (i == -1) {
            channel_selected = -2;
        }
    }

    public static void joinChannel() {
        SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);

        Mutable.IntegerValue sessionId = new Mutable.IntegerValue();

        mBus.enableConcurrentCallbacks();
        // method required which would
        short contactPort = CONTACT_PORT;
        while (channel_selected == -1) {
            try {
                Thread.sleep(500);
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

        flag = 2;
    }

    public static void run_client() throws BusException, InterruptedException {
        channels = new String[100];
        channel_count=0;
        channel_selected=-1;
        myInterface=null;
        mGroupInterface=null;
        mySignalInterface = new SampleSignalHandler();
        class MyBusListener extends BusListener {

            public void foundAdvertisedName(String name, short transport, String namePrefix) {
                System.out.println(String.format("BusListener.foundAdvertisedName(%s, %d, %s)", name, transport, namePrefix));
                channels[channel_count] = name.substring(29);
                channel_count++;
                flag2 = 1;
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

        //change made here
        //status = mBus.addMatch("type='signal'");
        //
        status = mBus.registerBusObject(mySignalInterface, "/chatService");
        status = mBus.findAdvertisedName(NAME_PREFIX);
        if (status != Status.OK) {
            return;
        }

        System.out.println("BusAttachment.findAdvertisedName successful " + "com.my.well.known.name");
        SignalInterface mySignalHandlers = new SignalInterface();

        status = mBus.registerSignalHandlers(mySignalHandlers);
        if (status != Status.OK) {
            return;
        }

        System.out.println("BusAttachment.registerSignalHandlers successful");

        groupSignalHandler mySampleService = new groupSignalHandler();

        status = mBus.registerBusObject(mySampleService, "/chatService");
        if (status != Status.OK) {
            return;
        }
        System.out.println("Method handler Registered");

        while (flag2 != 1) {
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
        while (flag != 2) {
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
