package ru.javatalks.chat.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import static ru.javatalks.chat.client.Client.CHARSET;

/**
 * ƒанный класс читает вход€щие сообщени€ сервера. —начала он их отсылает в
 * Client (по запросу), а затем просто печатает в консоль (после того, как
 *  лиент создаст отдельный поток на базе этого читател€)
 */
public class ClientReader implements Runnable {
	private BufferedReader br; // буфер чтени€ из сокета.
	private Socket s; // сокет, из которого бум читать

	/**
	 *  онструктор „итател€ клиента
	 * 
	 * @param socket
	 *            сокет, из которого читаем
	 * @throws IOException
	 *             это если не смогли создать буфер чтени€
	 */
	public ClientReader(Socket socket) throws IOException {
		s = socket;
		InputStreamReader isr = new InputStreamReader(s.getInputStream(),
				CHARSET);
		br = new BufferedReader(isr);
	}

	/**
	 * ћетод, который будет вызван после создани€ потока/нити на базе этого
	 * объекта
	 */
	public void run() {
		String line;
		// тупой цикл чтени€ пока сокет не закрыт.
		while (!s.isClosed() && ((line = readString()) != null)) {
			System.out.println(line); // и печати в консоль
		}

	}

	/**
	 * прочитать отдельную строку от сервера
	 * 
	 * @return строка-ответ сервера
	 */
	public String readString() {
		String line = "";
		try {
			line = br.readLine(); // читаем строку от сервера
		} catch (IOException e) {
			// печатаем стек-трейс только в случае необычных ошибок.
			if (!(e.getMessage().equalsIgnoreCase("Socket closed") || e
					.getMessage().equalsIgnoreCase("Connection reset"))) {
				e.printStackTrace();
			}
			// и пробуем закрыть сокет, если свалились по ошибке сюда.
			if (!s.isClosed()) {
				try {
					s.close();
				} catch (IOException ignore) {
				}
			}
			// а затем выходим вообще, если ошибка.
			System.exit(1);
		}
		return line;
	}

}
