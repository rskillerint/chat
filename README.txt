SMART JOYN feature 1 - Call Notification transfer from mobile to Desktop

The following is the implementation of feature 1 (for windows) of our project using Alljoyn libraries. The objective is to receive a call notification
from a mobile device running a similar application and being able to reject the call from the desktop

To run the code in Netbeans do the following
1 Create a netbeans project with the name org.alljoyn.bus.sample.chat
2 add all of the files to that project
3 open the properties tab for the project 
	under libraries add the folowing jar file (where your alljoyn library is)\java\jar\alljoyn
	under run add the following to the VM options
		-Djava.library.path=(where your alljoyn library is)/alljoyn-3.4.6-win7x86vs2010-sdk-dbg/java/lib/
		Windows user please note that the slash here and in windows file directory is different
4 If every thing goes fine running the App.java file will start the program