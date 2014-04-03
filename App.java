/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alljoyn.bus.sample.chat;

import org.alljoyn.bus.BusException;

/**
 *
 * @author admin
 */
public class App {
    static int ser_cli=0; 
    public static void type(int typ){
        ser_cli=typ;
    }
    public static void main(String[] args) throws BusException, InterruptedException {
        
         java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI_SmartJoyn().setVisible(true);
            }
        });
         
         while(true){
             while(ser_cli==0){
             try{
                 Thread.sleep(1000);
             }
             catch(Exception e){
                 System.out.println("There is an exception in App");
             }
         }
         if(ser_cli==1){
             Service.run_service();
             ser_cli=0;
         }
         if(ser_cli==2){
             Client.run_client();
             ser_cli=0;
         }
         }
    }
}

