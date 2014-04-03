/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.alljoyn.bus.sample.chat;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;


@BusInterface (name = "org.alljoyn.bus.samples.chat")
public interface ChatInterface {
    /*
     * The BusSignal annotation signifies that this function should be used as
     * part of the AllJoyn interface.  The runtime is smart enough to figure
     * out that this is a used as a signal emitter and is only called to send
     * signals and not to receive signals.
     */
    @BusSignal
    public void Chat(String str, String nickname) throws BusException;
    @BusSignal
    public void nickname(String usrname , String all_unique)throws BusException;
    @BusSignal
    public void validate(boolean val)throws BusException;
    @BusSignal
    public void sendKey(Double a)throws BusException;
    
}

