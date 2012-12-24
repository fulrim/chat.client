package ru.javatalks.chat.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import static ru.javatalks.chat.client.Client.CHARSET;

/**
 * ������ ����� ������ �������� ��������� �������. ������� �� �� �������� �
 * Client (�� �������), � ����� ������ �������� � ������� (����� ����, ���
 * ������ ������� ��������� ����� �� ���� ����� ��������)
 */
public class ClientReader implements Runnable {
	private BufferedReader br; // ����� ������ �� ������.
	private Socket s; // �����, �� �������� ��� ������

	/**
	 * ����������� �������� �������
	 * 
	 * @param socket
	 *            �����, �� �������� ������
	 * @throws IOException
	 *             ��� ���� �� ������ ������� ����� ������
	 */
	public ClientReader(Socket socket) throws IOException {
		s = socket;
		InputStreamReader isr = new InputStreamReader(s.getInputStream(),
				CHARSET);
		br = new BufferedReader(isr);
	}

	/**
	 * �����, ������� ����� ������ ����� �������� ������/���� �� ���� �����
	 * �������
	 */
	public void run() {
		String line;
		// ����� ���� ������ ���� ����� �� ������.
		while (!s.isClosed() && ((line = readString()) != null)) {
			System.out.println(line); // � ������ � �������
		}

	}

	/**
	 * ��������� ��������� ������ �� �������
	 * 
	 * @return ������-����� �������
	 */
	public String readString() {
		String line = "";
		try {
			line = br.readLine(); // ������ ������ �� �������
		} catch (IOException e) {
			// �������� ����-����� ������ � ������ ��������� ������.
			if (!(e.getMessage().equalsIgnoreCase("Socket closed") || e
					.getMessage().equalsIgnoreCase("Connection reset"))) {
				e.printStackTrace();
			}
			// � ������� ������� �����, ���� ��������� �� ������ ����.
			if (!s.isClosed()) {
				try {
					s.close();
				} catch (IOException ignore) {
				}
			}
			// � ����� ������� ������, ���� ������.
			System.exit(1);
		}
		return line;
	}

}
