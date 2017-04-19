/*
 * Implement a Client and Server Socket using a seperate theard and Threads Pool
 * @author Thuan Tran
 * @version 1
 * CSS 430 Question 4.27 and 4.28
 */


import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DateServer {

    private  static ServerSocket sock;
    public static void main(String[] args) {
        try {
        sock = new ServerSocket(6013);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        // comment the line below and uncomment the 2 line below(er) for 4.27
        // Using a Threads Pool
        ExecutorService threadsExecuter = Executors.newCachedThreadPool();

        while(true) {

                try {
                    Socket theClient = sock.accept();
                    HandleRequest theRequest = new HandleRequest(theClient);
                    threadsExecuter.execute(theRequest);
                    // Uncomment the 2 line above for question 4.27
                    // Create a seperate thread whenever a client access the Server

                    //            Thread seperateHandle = new Thread (theRequest);
                    //          seperateHandle.start();
                } catch (IOException ioe) {
                    System.err.println(ioe);
                }catch (NullPointerException nullPointer)
                {
                    // Open Task Manager and kill all java process.
                    // Then recompile and run the program again
                    System.out.println("Some thing is using the port, kill all java processes in  " +
                            "Task Manager");
                    break;
 }

            }
        }

    /*
        A private class for handling the request.
     */
    private static class HandleRequest implements Runnable {
        private Socket client;

        public HandleRequest(Socket theSocket) {
            this.client = theSocket;
        }

        public void run() {
            try {
                // Transfer info to the cline
                PrintWriter pout = new PrintWriter(this.client.getOutputStream(), true);
                pout.println((new Date()).toString());
                this.client.close();
            } catch (IOException ioe) {
                System.err.println(ioe);
            }

        }
    }
}
