package lab1;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client2 {
	Socket socket;
	private String username;
	// Socket serverSocket;
	String serverHostName = "localhost";
	int serverPortNumber = 7777;
	ServerListener sl;
	String message;
	Image image = null;
	boolean isMessage;
	boolean isAdmin = false;

	Client2(String ipAddr, int serverPort) {
		// get username
		Scanner in = new Scanner(System.in);
		System.out.print("Please enter a username: ");
		this.username = in.nextLine().toLowerCase();
		// in.close();
		int msgCount = 0;
		if (username.equals("admin")) {
			isAdmin = true;
		}

		PrintWriter out;
		// connect to server2
		try {
			// connect to socket
			int option = 0;
			socket = new Socket(ipAddr, serverPort);

			// write username to socket with PrintWriter
			out = new PrintWriter(this.socket.getOutputStream(), true);
			out.println(this.username);

			// ask for type of data to send
			if (isAdmin == false) {
				System.out.print("Are you sending a message(0) or image(1)? (0/1): ");
				option = in.nextInt();
				if (option == 0) {
					isMessage = true;
				} else {
					isMessage = false;
				}

				// get message as string or image file
				if (isMessage == true) {
					System.out.println("Please enter message as a string: ");
					message = in.next();
					// send message to Server2 via PrintWriter
					String encryptedMessage = encrypt(message);
					//msgCount++;
					//updateTextFile(encryptedMessage, msgCount);
					System.out.println(encryptedMessage);
					out.println(encryptedMessage);
					//out.println(msgCount);
					out.flush();
				} else {
					System.out.println("Please enter directory path to image. ");
					String filePath = in.next();
					FileInputStream fis = new FileInputStream(filePath);
					byte[] buffer = new byte[fis.available()];
					fis.read(buffer);
					String s = encryptImage(buffer);
					//msgCount++;
					//updateTextFile(s, msgCount);
					ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
					outToServer.writeObject(s);
					//outToServer.writeInt(msgCount);
					outToServer.flush();
				}
			} else {
				System.out.println("Hello admin, please select from the following commands: ");
				System.out.println("(1) Broadcast message to all clients");
				System.out.println("(2) List messages in chat.txt");
				System.out.println("(3) Delete message number in chat.txt");
				System.out.print("Selection (1/2/3): ");
				option = in.nextInt();
			}
			if (option == 1) {
				System.out.println("Broadcast message to all clients: ");
				System.out.print("Enter message: ");
				String message = in.nextLine();
			}
			else if (option == 2) {
				//Display chat.txt log
			}
			else if (option == 3) {
				System.out.println("Please enter a message number to delete. ");
				int deleteNum = in.nextInt();
				deleteMessage(deleteNum);
				System.out.print("Message "+ deleteNum + " deleted.");
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// create serverListener
		sl = new ServerListener(this, this.socket);
		new Thread(sl).start();

		in.close();

	}

	private static String encryptImage(byte[] bArray) {

		int mask1 = 0xFC;
		int mask2 = 0x03;
		int mask3 = 0xF0;
		int mask4 = 0x0F;
		int mask5 = 0xC0;
		int mask6 = 0x3F;
		int[] numArray = new int[bArray.length];

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bArray.length - 2; i += 3) {

			// grabbing 3 bytes to turn into 6 bit sections
			byte b1 = bArray[i];
			byte b2 = bArray[i + 1];
			byte b3 = bArray[i + 2];

			/*
			 * System.out.print("bHex#: " + Integer.toHexString(b1));
			 * System.out.print(" - " + Integer.toHexString(b2));
			 * System.out.println(" - " + Integer.toHexString(b3));
			 */

			// using bit masks to extract the bit sections
			int val1 = b1 & mask1;
			int val2 = b1 & mask2;
			int val3 = b2 & mask3;
			int val4 = b2 & mask4;
			int val5 = b3 & mask5;
			int val6 = b3 & mask6;

			// merging bits into the sections in correct order and position
			int out1 = val1 >>> 2;
			int out2 = (val2 << 4) | (val3 >>> 4);
			int out3 = (val4 << 2) | (val5 >>> 6);
			int out4 = val6;

			/*
			 * System.out.println("Encrypt fragments\n" + " --: " +
			 * bitString(0x00) + " V2: " + bitString(val2) + " V4: " +
			 * bitString(val4) + " --: " + bitString(0x00) );
			 * 
			 * System.out.println("" + " v1: " + bitString(val1) + " v3: " +
			 * bitString(val3) + " v5: " + bitString(val5) + " v6: " +
			 * bitString(val6) );
			 * 
			 * System.out.println("" + " o1: " + bitString(out1) + " o2: " +
			 * bitString(out2) + " o5: " + bitString(out3) + " o6: " +
			 * bitString(out4) );
			 */

			/*
			 * System.out.println("Hex #: " + Integer.toHexString(out1) + " - "
			 * + Integer.toHexString(out2) + " - " + Integer.toHexString(out3) +
			 * " - " + Integer.toHexString(out4)); sb.append((char) out1);
			 * sb.append((char) out2); sb.append((char) out3); sb.append((char)
			 * out4); System.out.print("sHex#: " +
			 * Integer.toHexString(sb.charAt(0))); System.out.print(" - " +
			 * Integer.toHexString(sb.charAt(1))); System.out.print(" - " +
			 * Integer.toHexString(sb.charAt(2))); System.out.println(" - " +
			 * Integer.toHexString(sb.charAt(3))); System.out.println(
			 * "Encrypted string: " + sb.toString());
			 */
		}

		return sb.toString();
	}

	private static String encrypt(String input) {
		String msg = input;
		char key = 0xF0;
		String str = Integer.toBinaryString(key);
		String text = "";
		int xor;
		char c, tmp;
		System.out.println("");
		for (int i = 0; i < msg.length(); i++) {
			//System.out.println(i);
			c = msg.charAt(i);
			xor = c ^ key;
			tmp = (char) xor;
			text = text + tmp;
		}
		return text;
	}

	public static void main(String[] args) {
		Client2 lc = new Client2("localhost", 7777);
	}

	class ServerListener implements Runnable {
		Client2 lc;
		Scanner in; // this is used to read which is a blocking call

		ServerListener(Client2 lc, Socket s) {
			try {
				this.lc = lc;
				in = new Scanner(new BufferedInputStream(s.getInputStream()));
				//System.out.println(in.nextLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while (true) { // run forever
				//System.out.println("Client - waiting to read");
				//String cmd = in.next();
				if(in.hasNext()){
				String s = in.nextLine();
				lc.handleMessage(s);
				} 
				
			}

		}
	}

	public void handleMessage(String s) {
		// switch (cmd) {
		// case "print":
		System.out.println("client side: " + s);
		// break;
		// case "exit":
		// System.exit(-1);
		// break;
		// default:
		// System.out.println("client side: unknown command received:" + cmd);
	}


	public static void deleteMessage(int msgIndex) throws IOException {

		String txtFilePath = "chat.txt";
		File fileName = new File(txtFilePath);
		File tmpFile = new File("tmp.txt");
		tmpFile.createNewFile();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(tmpFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Scanner scan = null;
		try {
			scan = new Scanner(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (scan.hasNextLine()) {
			String line = scan.nextLine().trim();
			String index = line.substring(0, line.indexOf(" "));
			int indexNum = Integer.parseInt(index);
			if (indexNum == msgIndex) {
				continue;
			}
			try {
				writer.write(line + System.getProperty("line.separator"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		writer.close();
		scan.close();
		tmpFile.renameTo(fileName);
	}
}
