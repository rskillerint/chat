/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.alljoyn.bus.sample.chat;

import java.util.ArrayList;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

@BusInterface (name = "org.alljoyn.bus.samples.GroupInterface")
public interface GroupInterface {
    
    @BusMethod
    public double askKey()throws BusException;
    @BusMethod
    public String[] getMem() throws BusException;
    @BusMethod 
    public String[] getUni() throws BusException;
}
