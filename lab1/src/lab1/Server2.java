package lab1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server2 implements Runnable {

	private ServerSocket serverSocket = null;
	private Thread mainThread = null;
	private File file = new File("chat.txt");
	private PrintWriter writer;
	private int msgCount = 0;
	private int port;
	private String clientName;
	private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
	private ClientHandler client;
	public boolean writeToAll = false;

	public Server2() {
		port = 7777;
		try {
			// create socket
			serverSocket = new ServerSocket(port);
			System.out.println("Server started: " + serverSocket + "At port " + port);

			// clear previous chat log
			try (PrintWriter pw = new PrintWriter("chat.txt")) { pw.close();
			} catch (IOException e) {}

		} catch (IOException ioe) {
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
			System.exit(-1);
		}
	}

	@Override
	public void run() {
		PrintWriter out;
		Scanner in;

		while (true) {
			Socket clientSocket = null;
			try {
				// wait for client to connect to server
				clientSocket = serverSocket.accept();

				in = new Scanner(new BufferedInputStream(clientSocket.getInputStream()));

				// Get clientName from Client2
				clientName = in.nextLine();

				// if clientName is admin, send message to writeToAll() method
				// instead of write() method
				if (clientName.equals("admin") && clientList.size() > 0) {
					writeToAll = true;
					while (!in.hasNextLine()) {
					}
					String message = in.nextLine();
					for (ClientHandler c : clientList) {
						System.out.println(c.clientName);
						msgCount++;
						c.write(clientSocket, c.clientName, message, msgCount);
					}
				} else {

					// spawn new thread to handle client request
					// pass name to client handler, increment msgCount as unique
					// identifier for message
					msgCount++;
					client = new ClientHandler(clientSocket, clientName, msgCount);// ,
																					// writeToAll);

					// add client to list of clients
					clientList.add(client);
					Thread t = new Thread(client);
					t.start();

				}
			} catch (IOException e) {
				System.out.println("Accept failed: " + this.port);
				System.exit(-1);
			}
		}

	}

	public static void main(String[] args) throws IOException {
		Server2 server = new Server2();
		(new Thread(server)).start();

	} // end of main method

	void writeToAll(Socket socket, ArrayList<ClientHandler> list, String message, int messageCount) {

	}
} // end of class MyServer

class ClientHandler implements Runnable {
	Socket s; // this is socket on the server side that connects to Client2
	int num; // keeps track of its number just for identifying purposes
	String clientName;
	private int msgCount;
	// private boolean writeToAll;
	String[] clients;

	ClientHandler(Socket s, String name, int count) {// , boolean writeAll) {
		this.s = s;
		clientName = name;
		msgCount = count;
		// this.writeToAll = writeAll;
	}

	// This is the client handling code
	// This keeps running handling client requests after initially sending some
	// stuff to the client
	public void run() {
		Scanner in;
		PrintWriter out;
		String message;
		try {
			// 1. GET SOCKET IN/OUT STREAMS
			in = new Scanner(new BufferedInputStream(s.getInputStream()));
			out = new PrintWriter(new BufferedOutputStream(s.getOutputStream()));

			// 2. Get clientName
			// this is because getting the next line is not a blocking call
			// the while loop is saying to wait until we DO have a next line,
			// i.e. the message
			while (true) {
				while (!in.hasNextLine()) {
				}
				message = in.nextLine();
				// msgCount = in.nextInt();

				// PRINT SOME STUFF TO THE CLIENT
				// out.println("print Hello There"+ clientName);
				// out.println
				// System.out.println(("Message: " + message + " Message count:
				// " + msgCount));
				out.println(("Message: " + message + " Message count: " + msgCount));
				out.flush();

				// write message to server
				// if(writeToAll == false){
				write(s, clientName, message, msgCount);
				msgCount++;
			}
			// 3. KEEP LISTENING AND RESPONDING TO CLIENT REQUESTS
			// while (true) {
			// System.out.println("Server - waiting to read");
			// while(!in.hasNextLine()){}
			// String clientMessage = in.nextLine();
			// handleRequest(clientName,count);
			// count++;
			// }
			// out.println("exit done with wishes");
			// out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// This handling code dies after doing all the printing
	} // end of method run()

	void handleRequest(String s, int count) {
		clients[count] = s;
	}

	void write(Socket socket, String clientName, String message, int messageCount) {
		Thread t = new Thread(new ClientThread(socket, clientName, message, messageCount));
		t.start();
	}

} // end of class ClientHandler

class ClientThread implements Runnable {
	private String message;
	private String clientName;
	private int msgCount;
	PrintWriter out;
	Socket socket;

	public ClientThread(Socket s, String name, String msg, int count) {
		this.message = msg;
		this.clientName = name;
		socket = s;
		msgCount = count;
	}

	@Override
	public void run() {

		try {

			out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
			out.println("Server received: " + message);
			out.flush();
			String decryptedMessage = decrypt(message);
			out.println("Writing " + decryptedMessage + " to chat.txt.");
			out.flush();

			updateTextFile(decryptedMessage, clientName, msgCount);
			// msgCount++;

			// out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void updateTextFile(String s, String clientName, int msgCount) {

		// hard code in text file path
		String txtFilePath = "chat.txt";
		// File fileName = new File(txtFilePath);

		try (FileWriter fw = new FileWriter(txtFilePath, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			String msgWithNum = msgCount + " " + clientName + " " + s;
			out.println(msgWithNum);

			//fw.close();
			//bw.close();
			//out.close();
		} catch (IOException e) {
		}

	}

	private static String decrypt(String input) {
		String txt = input;
		char key = 0xF0;
		String msg = "";
		int xor;
		char c, tmp;
		for (int i = 0; i < txt.length(); i++) {
			c = txt.charAt(i);
			xor = c ^ key;
			tmp = (char) xor;
			msg = msg + tmp;
		}
		return msg;
	}

	private static String decryptImage(String s) {

		int mask1 = 0x3F; // 0011 1111
		int mask2 = 0x30; // 0011 0000
		int mask3 = 0x0F; // 0000 1111
		int mask4 = 0x3C; // 0011 1100
		int mask5 = 0x03; // 0000 0011
		int mask6 = 0x3F; // 0011 1111

		byte b1, b2, b3, b4;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length() - 3; i += 4) {
			// System.out.println("s1: "+ s.charAt(i) + "s2: " + s.charAt(i+1) +
			// "s3: " + s.charAt(i + 2) + "s4: " + s.charAt(i+3));
			b1 = (byte) s.charAt(i);
			b2 = (byte) s.charAt(i + 1);
			b3 = (byte) s.charAt(i + 2);
			b4 = (byte) s.charAt(i + 3);

			/*
			 * System.out.print("dHex#: " + Integer.toHexString(b1));
			 * System.out.print(" - " + Integer.toHexString(b2));
			 * System.out.print(" - " + Integer.toHexString(b3));
			 * System.out.println(" - " + Integer.toHexString(b4));
			 */

			int val1 = b1 & mask1;
			int val2 = b2 & mask2;
			int val3 = b2 & mask3;
			int val4 = b3 & mask4;
			int val5 = b3 & mask5;
			int val6 = b4 & mask6;

			// System.out.println("v: " + val1 +" - "+ val2 +" - "+ val3 +" - "+
			// val4 +" - "+ val5 +" - "+ val6);

			// merging bits into the sections in correct order and position
			int out1 = (val1 << 2) | (val2 >>> 4);
			int out2 = (val3 << 4) | (val4 >>> 2);
			int out3 = (val5 << 6) | val6;

			/*
			 * System.out.println("Decrypt fragments\n" + " V1: " +
			 * bitString(val1) + " V3: " + bitString(val3) + " V5: " +
			 * bitString(val5) + "\n" + " v2: " + bitString(val2) + " v4: " +
			 * bitString(val4) + " v6: " + bitString(val6) + "\n" + " o1: " +
			 * bitString(out1) + " o2: " + bitString(out2) + " o3: " +
			 * bitString(out3));
			 */

			System.out.println("Hexes: " + out1 + " -- " + out2 + " -- " + out3);
			char c1 = (char) out1;
			char c2 = (char) out2;
			char c3 = (char) out3;
			sb.append(c1);
			sb.append(c2);
			sb.append(c3);
			// System.out.println("c1: " + c1 + "c2: " + c2 + "c3: " + c3);
			// System.out.println("Decrypting output in progress: " +
			// sb.toString());
		}
		return sb.toString();
	}
} // end of class ClientThread
