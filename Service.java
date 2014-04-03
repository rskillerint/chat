/*
 * Copyright (c) 2010-2011, 2013, AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.alljoyn.bus.sample.chat;

import java.util.Scanner;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.MessageContext;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignalHandler;

public class Service {

    static {
        System.loadLibrary("alljoyn_java");
    }

    public static void sendMessage(String s) throws BusException {
        myInterface.Chat(s, nickname[0]);
    }

    // Start of variable Declarations
    private static final String NAME_PREFIX = "org.alljoyn.bus.samples.chat";
    private static final short CONTACT_PORT = 27;
    private static BusAttachment mBus;
    static int mUseSessionId = -1;
    private static double key = (Math.random() * 100000);
    private static String[] all_uni = new String[100];
    private static int name_count = 0;
    static String[] nickname = new String[100];
    private static double[] keys = new double[100];
    private static int key_count = 0;
    static boolean mSessionEstablished = false;
    static int mSessionId;
    static String mJoinerName;
    static ChatInterface myInterface = null;
    static String s = null;

    //End of Variable Declarations
    public static class SignalInterface implements ChatInterface, BusObject {

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

    public static class SignalHandler {

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "Chat")
        public void Chat(String string, String nick) {

            String uniqueName = mBus.getUniqueName();
            MessageContext ctx = mBus.getMessageContext();

            if (ctx.sender.equals(uniqueName)) {
                System.out.println("Chat(): dropped our own signal received on session " + ctx.sessionId);
                return;
            }

            /*
             * To keep the application simple, we didn't force users to choose a
             * nickname.  We want to identify the message source somehow, so we
             * just use the unique name of the sender's bus attachment.
             */
            String nickname = ctx.sender;
            nickname = nickname.substring(nickname.length() - 10, nickname.length());
            String as = nick + " -> " + string;
            System.out.println(nickname + ": " + as);
            new messageTh(as).start();

        }

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "nickname")
        public void nickname(String usrname, String all_unique) throws BusException {
            System.out.println("!!!Validation is called!!!");
            int contain = 0;
            for (int i = 0; i < 100; i++) {
                if (nickname[i] != usrname) {
                    contain = 1;
                    break;
                }
            }
            if (contain == 1) {
                nickname[name_count] = usrname;
                all_uni[name_count] = all_unique;
                name_count++;
                myInterface.validate(true);

            } else {
                myInterface.validate(false);
            }

        }

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "validate")
        public void validate(boolean val) {
        }

        @BusSignalHandler(iface = "org.alljoyn.bus.samples.chat", signal = "sendKey")
        public void sendKey(Double a) {
            keys[key_count] = a;
            key_count++;
        }
    }

    public static class groupSignalHandler implements GroupInterface, BusObject {

        public void preDispatch() {
        }

        public void postDispatch() {
        }

        @Override
        public synchronized double askKey() {
            return key;
        }

        @Override
        public synchronized String[] getMem() throws BusException {
            return nickname;
        }

        @Override
        public synchronized String[] getUni() throws BusException {
            return all_uni;
        }

    }

    private static class MyBusListener extends BusListener {

        public void nameOwnerChanged(String busName, String previousOwner, String newOwner) {
            if ("com.my.well.known.name".equals(busName)) {
                System.out.println("BusAttachement.nameOwnerChanged(" + busName + ", " + previousOwner + ", " + newOwner);
            }
        }
    }

    public static void read(String text) {
        s = text;
    }

    public static void run_service() throws BusException {
        for (int i = 0; i < 100; i++) {
            all_uni[i] = "";
            nickname[i] = "";
        }
        mBus = new BusAttachment("org.alljoyn.bus.samples", BusAttachment.RemoteMessage.Receive);

        Status status;

        final SignalInterface mySignalInterface = new SignalInterface();

        status = mBus.registerBusObject(mySignalInterface, "/chatService");
        if (status != Status.OK) {
            return;
        }
        System.out.println("BusAttachment.registerBusObject successful");

        BusListener listener = new MyBusListener();
        mBus.registerBusListener(listener);

        status = mBus.connect();
        if (status != Status.OK) {
            return;
        }
        System.out.println("BusAttachment.connect successful on " + System.getProperty("org.alljoyn.bus.address"));

        SignalHandler mySignalHandlers = new SignalHandler();

        status = mBus.registerSignalHandlers(mySignalHandlers);
        if (status != Status.OK) {

            return;
        }
        System.out.println("Signal Handler registered");

        groupSignalHandler mySampleService = new groupSignalHandler();

        status = mBus.registerBusObject(mySampleService, "/chatService");
        if (status != Status.OK) {
            System.out.println(status);
            return;
        }
        System.out.println("Method handler Registered");

        all_uni[name_count] = mBus.getUniqueName();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a nick name");
        nickname[name_count] = scanner.nextLine();
        name_count++;

        Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);

        SessionOpts sessionOpts = new SessionOpts();
        sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
        sessionOpts.isMultipoint = true;
        sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
        sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

        status = mBus.bindSessionPort(contactPort, sessionOpts,
                new SessionPortListener() {
                    public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
                        System.out.println("SessionPortListener.acceptSessionJoiner called");
                        if (sessionPort == CONTACT_PORT) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    public void sessionJoined(short sessionPort, int id, String joiner) {
                        System.out.println(String.format("SessionPortListener.sessionJoined(%d, %d, %s)", sessionPort, id, joiner));
                        mSessionId = id;
                        mJoinerName = joiner;
                        System.out.println(mJoinerName);
                        mSessionEstablished = true;
                        mUseSessionId = id;

                        SignalEmitter emitter = new SignalEmitter(mySignalInterface, mSessionId, SignalEmitter.GlobalBroadcast.Off);
                        myInterface = emitter.getInterface(ChatInterface.class);
                    }
                });
        if (status != Status.OK) {
            return;
        }
        System.out.println("BusAttachment.bindSessionPort successful");

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Create_Channel().setVisible(true);
            }
        });
        while (s == null) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
        System.out.println(s);

        String wellKnownName = NAME_PREFIX + "."+s;
        int flags = 0; //do not use any request name flags
        status = mBus.requestName(wellKnownName, flags);
        if (status != Status.OK) {
            return;
        }
        System.out.println("BusAttachment.request 'com.my.well.known.name' successful");

        status = mBus.advertiseName(wellKnownName, SessionOpts.TRANSPORT_ANY);
        if (status != Status.OK) {
            System.out.println("Status = " + status);
            mBus.releaseName(wellKnownName);
            return;
        }
        System.out.println("BusAttachment.advertiseName 'com.my.well.known.name' successful");

        try {
            while (!mSessionEstablished) {
                Thread.sleep(10);

            }
            System.out.println("Server running");
            while (true) {

                Thread.sleep(50000);
                myInterface.Chat("service_message", nickname[0]);
            }
        } catch (InterruptedException ex) {
            System.out.println("Interrupted");
        }
    }

    public static void main(String[] args) throws BusException {

    }
}

class messageTh extends Thread {

    final String f;

    public messageTh(String s) {
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
