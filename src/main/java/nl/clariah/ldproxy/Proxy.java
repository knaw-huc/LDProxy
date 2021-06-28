/*
 * based on https://github.com/stefano-lupo/Java-Proxy-Server
 */

package nl.clariah.ldproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import nl.clariah.ldproxy.recipe.Recipe;
import nl.mpi.tla.util.Saxon;


/**
 * The Proxy creates a Server Socket which will wait for connections on the specified port.
 * Once a connection arrives and a socket is accepted, the Proxy creates a RequestHandler object
 * on a new thread and passes the socket to it to be handled.
 * This allows the Proxy to continue accept further connections while others are being handled.
 * 
 * The Proxy class is also responsible for providing the dynamic management of the proxy through the console 
 * and is run on a separate thread in order to not interrupt the acceptance of socket connections.
 * This allows the administrator to dynamically block web sites in real time. 
 * 
 * The Proxy server is also responsible for maintaining cached copies of the any websites that are requested by
 * clients and this includes the HTML markup, images, css and js files associated with each webpage.
 * 
 * Upon closing the proxy server, the HashMaps which hold cached items and blocked sites are serialized and
 * written to a file and are loaded back in when the proxy is started once more, meaning that cached and blocked
 * sites are maintained.
 *
 */
public class Proxy implements Runnable{


	// Main method for the program
	public static void main(String[] args) {
		// Create an instance of Proxy and begin listening for connections
		Proxy myProxy = new Proxy(8085);
		myProxy.listen();	
	}


	private ServerSocket serverSocket;

	/**
	 * Semaphore for Proxy and Console Management System.
	 */
	private volatile boolean running = true;

	/**
	 * ArrayList of threads that are currently running and servicing requests.
	 * This list is required in order to join all threads on closing of server
	 */
	static ArrayList<Thread> servicingThreads;

	/**
	 * Data structure for constant order lookup of blocked sites.
	 * Key: URL of page/image requested.
	 * Value: URL of page/image requested.
	 */
	static HashMap<Pattern,Recipe> ldsites;

	/**
	 * Create the Proxy Server
	 * @param port Port number to run proxy server from.
	 */
	public Proxy(int port) {

		ldsites = new HashMap<>();

		// Create array list to hold servicing threads
		servicingThreads = new ArrayList<>();

		// Start dynamic manager on a separate thread.
		new Thread(this).start();	// Starts overriden run() method at bottom

			// Load LD proxy sites
			try {
				File config = new File("src/resources/ldproxy-config.xml");
				XdmNode conf = Saxon.buildDocument(new StreamSource(config));

				for (XdmItem s:Saxon.xpathList(conf, "/LDProxy/site")) {
					if (Saxon.hasAttribute(s, "match")) {
                                                String  m =  Saxon.xpath2string(s, "@match");
						Pattern p = Pattern.compile(m);

						if (Saxon.hasAttribute(s, "recipe")) {
                                                        String c = Saxon.xpath2string(s, "@recipe");
							Class<Recipe> clazz = (Class<Recipe>) Class.forName(c);
                                                        Recipe r = clazz.newInstance();
							r.init(s);
							System.err.println("!DBG: site["+m+"] recipe["+c+"]");
							this.ldsites.put(p,r);
						} else {
							System.err.println("!ERR: site["+m+"] misses a recipe attribute!");
						}
					} else {
						System.err.println("!ERR: site misses a match attribute!");
					}
				}

			} catch(Exception e) {
				System.out.println("!ERR: couldn't load the LDProxy config! \n"+e.getMessage());
				e.printStackTrace(System.out);
			}



		try {
			// Create the Server Socket for the Proxy 
			serverSocket = new ServerSocket(port);

			// Set the timeout
			//serverSocket.setSoTimeout(100000);	// debug
			System.err.println("?DBG: Waiting for client on port " + serverSocket.getLocalPort() + "..");
			running = true;
		} 

		// Catch exceptions associated with opening socket
		catch (SocketException se) {
			System.err.println("!ERR: Socket Exception when connecting to client: "+se.getMessage());
			se.printStackTrace(System.err);
		}
		catch (SocketTimeoutException ste) {
			System.err.println("!ERR: Timeout occured while connecting to client: "+ste.getMessage());
		} 
		catch (IOException io) {
			System.err.println("!ERR: IO exception when connecting to client: "+io.getMessage());
		}
	}


	/**
	 * Listens to port and accepts new socket connections. 
	 * Creates a new thread to handle the request and passes it the socket connection and continues listening.
	 */
	public void listen(){

		while(running){
			try {
				// serverSocket.accpet() Blocks until a connection is made
				Socket socket = serverSocket.accept();
				
				// Create new Thread and pass it Runnable RequestHandler
				Thread thread = new Thread(new RequestHandler(socket));
				
				// Key a reference to each thread so they can be joined later if necessary
				servicingThreads.add(thread);
				
				thread.start();	
			} catch (SocketException e) {
				// Socket exception is triggered by management system to shut down the proxy 
				System.out.println("Server closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Joins all of the RequestHandler threads currently servicing requests.
	 */
	private void closeServer(){
		System.out.println("\nClosing Server..");
		running = false;
			try{
				// Close all servicing threads
				for(Thread thread : servicingThreads){
					if(thread.isAlive()){
						System.out.print("Waiting on "+  thread.getId()+" to close..");
						thread.join();
						System.out.println(" closed");
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			// Close Server Socket
			try{
				System.out.println("Terminating Connection");
				serverSocket.close();
			} catch (Exception e) {
				System.err.println("!ERR: Exception closing proxy's server socket: "+e.getMessage());
				e.printStackTrace(System.err);
			}

		}


		/**
		 * Creates a management interface
                 *  	close	: Closes the proxy server
		 */
		@Override
		public void run() {
			Scanner scanner = new Scanner(System.in);

			String command;
			while(running){
				System.out.println("Enter \"close\" to close server.");
				command = scanner.nextLine();
				if(command.equals("close")){
					running = false;
					closeServer();
				}
			}
			scanner.close();
		} 

	}
