package ru.javatalks.chat.client;

import java.net.Socket;
import java.io.*;


/**
 * ����� �������. ��� ���� ���������, �� ����� ����. ������ ���������
 * ������������ � ������� ������� � ����� � ��������.
 */
public class Client {
	public static final String CHARSET = "UTF-16";	// ���������, ������ ���
													// ������� � ��������.
	private static final String OFFER_NAME = "OFFER NAME:";	// ��������� �������
															// ������� �
															// ������������
															// �����.
	private static final String REGISTER_SUCCESSFULL = "Register successfull!";	// ������
																				// ���������.
	private Socket socket; // ��� ����� � �������
	private String name; // ���� ��� � ������ �������������
	private BufferedWriter bw; // �������� � �����
	private BufferedReader userInput; // �������� � ������� (����������������
										// ����)

	// ����������� ���������� �������
	// �������� ���� � ���� �� ���������
	// ���������� ������������ ������ ���� ������ ��� ��������������� ��
	// ���������
	public Client(String host, int port) throws IOException {
		// ������������ �������� � �������
		userInput = new BufferedReader(new InputStreamReader(System.in));
		// ���������� ������ ����� (IP ������)
		System.out.println("Type server IP host (hit Enter for \"" + host
				+ "\")");
		String userAnswer;
		userAnswer = userInput.readLine();
		if (userAnswer.length() != 0) {
			host = userAnswer;
		}
		// ���������� ������������ ������ �����
		System.out.println("Type server port (hit Enter for \"" + port + "\")");
		userAnswer = userInput.readLine();
		if (userAnswer.length() != 0) {
			int newPort = Integer.parseInt(userAnswer);
			// ���� ������������ ���� ���������� �������� ������ - ��������� ��
			// ����������������
			if (newPort > 1024 && newPort < 65535) {
				port = newPort;
			}
		}

		System.out.println("Trying connect to " + host + ":" + port + "...");
		// ������� ��������������
		socket = new Socket(host, port);
		// ���� ���������� - ������������ �������� �� �������� ������ ������
		bw = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream(), CHARSET));

	}

	/**
	 * ����� ����� �������� ������
	 */
	public void close() {
		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ����������� �������.
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	/**
	 * ��������� ������. � ������ ������������� ������ ����� ������, ����
	 * readLine() ������� ��������� �������� ���������
	 * 
	 * @param s
	 *            ������ � ��������
	 * @throws IOException
	 */
	public void writeString(String s) throws IOException {
		bw.write(s);
		bw.write('\n');
		bw.flush();
	}

	/**
	 * ����� ���� ��� � ������ �������������
	 * 
	 * @return ��� ������������
	 */
	public String getName() {
		return name;
	}

	/**
	 * ������� ���� ��������� ����� ������������ ��� ������������, �������
	 * �������������� �� �������. ����� �� ��������, ��� ������� ������������ �
	 * ������� � ������������ �� ������
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		// ������� ������ ClientReader'a, ���������� ������, ������� �����
		// ������ ��������� � ������� � ���������� ���
		// ��� �� �������
		final ClientReader clientReader = new ClientReader(socket);
		System.out.println("Type your name, please (or hit Enter to exit):");
		do {// ��� ����, ��� ������������ ����� �� "�������" �� ��������� ��� ��
			// � �������, �� �� �������... ����
			name = userInput.readLine(); // ������ ������������ ������������� �
											// ������� ���
			if (name.equals("")) { // ���� ������������ ������ �� �������
									// ����������...
				close();
				System.exit(1); // �������
			}
			writeString(OFFER_NAME + name); // ����������� ������� ������� �
											// ���, ��������� �������
			String answer = clientReader.readString(); // ������� ����� ��
														// �������
			System.out.println(answer); // �������� ����� � �������
			if (answer.indexOf(REGISTER_SUCCESSFULL) != -1) { // ����
																// �����������
																// �� �������
																// �������
				break; // ...������� � ����� ��������� �����.
			}
		} while (true);
		// ��������� ClientReader'a � ��������� ������.
		// ������ �� ����� ���� ��������������� ���������� �� ������� �� �������
		new Thread(clientReader, "ClientReader").start();

		String s;
		// � ����� � � ��������� ��� ������� ����
		// "������ ������������ - ������� �������, ���� ������ ��������"
		while (!(s = userInput.readLine()).equals("")) {
			writeString(s);
		}
		// � ��� ���� ������������ ������ ����� Enter, �� ����� �����, ���
		// �������, ��� �� ����� ����� � ���������
		System.out.println("Bye-bye!");

	}

	/**
	 * ��, main �� main � ����. ������� �����.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Client c = null;
		try {
			// ������� ������� � ����������� �� ���������, �������
			// ���������������.
			c = new Client("localhost", 45000);
			c.run(); // ��������� �� ������� �������, ���� ������������ ������.
		} catch (IOException e) {
			// ���� ������� ��� �� �����/�����, �� ������� ������ ��� ���������
			if (e.getMessage().indexOf("Connection refused") == 0) {
				System.out
						.println("Unable to connect (Server is not running?)");
			} else if (!(e.getMessage().equalsIgnoreCase("Socket closed") || e
					.getMessage().equalsIgnoreCase("Connection reset"))) {
				// ���� ������� ������ ���� ���-�� ���� �� ����� ���������. ��
				// �������� ������ �������� ��� �������� ����.
				e.printStackTrace();
			}
		} finally {
			if (c != null) {
				c.close(); // ��������� ������� ������� �������. :)
			}
		}

	}
}