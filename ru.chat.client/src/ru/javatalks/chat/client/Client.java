package ru.javatalks.chat.client;

import java.net.Socket;
import java.io.*;


/**
 * Класс Клиента. Это лишь заготовка, не более того. Однако позволяет
 * коннектиться к некоему серверу в инете и общаться.
 */
public class Client {
	public static final String CHARSET = "UTF-16";	// кодировка, единая для
													// сервера и клиентов.
	private static final String OFFER_NAME = "OFFER NAME:";	// заголовок команды
															// клиента с
															// предложением
															// имени.
	private static final String REGISTER_SUCCESSFULL = "Register successfull!";	// просто
																				// константа.
	private Socket socket; // наш сокет к серверу
	private String name; // наше имя в списке пользователей
	private BufferedWriter bw; // писатель в сокет
	private BufferedReader userInput; // читатель с консоли (пользовательский
										// ввод)

	// конструктор экземпляра клиента
	// получает хост и порт по умолчанию
	// предлагает пользователю ввести свои данные или воспользоваться по
	// умолчанию
	public Client(String host, int port) throws IOException {
		// конструируем читателя с консоли
		userInput = new BufferedReader(new InputStreamReader(System.in));
		// опрашиваем насчет хоста (IP адреса)
		System.out.println("Type server IP host (hit Enter for \"" + host
				+ "\")");
		String userAnswer;
		userAnswer = userInput.readLine();
		if (userAnswer.length() != 0) {
			host = userAnswer;
		}
		// спрашиваем пользователя насчет порта
		System.out.println("Type server port (hit Enter for \"" + port + "\")");
		userAnswer = userInput.readLine();
		if (userAnswer.length() != 0) {
			int newPort = Integer.parseInt(userAnswer);
			// если пользователь ввел допустимый диапазон портов - подменяем на
			// пользовательский
			if (newPort > 1024 && newPort < 65535) {
				port = newPort;
			}
		}

		System.out.println("Trying connect to " + host + ":" + port + "...");
		// пробуем приконнетиться
		socket = new Socket(host, port);
		// если получилось - конструируем писателя на выходном потоке сокета
		bw = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream(), CHARSET));

	}

	/**
	 * Общий метод закрытия сокета
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

	// финализатор клиента.
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	/**
	 * Отправить строку. К строке приписывается символ новой строки, дабы
	 * readLine() сервера корректно прочитал сообщение
	 * 
	 * @param s
	 *            Строка к отправке
	 * @throws IOException
	 */
	public void writeString(String s) throws IOException {
		bw.write(s);
		bw.write('\n');
		bw.flush();
	}

	/**
	 * взять наше имя в списке пользователей
	 * 
	 * @return Имя пользователя
	 */
	public String getName() {
		return name;
	}

	/**
	 * главный цикл программы Здесь определяется имя пользователя, пытаеся
	 * регистрануться на сервере. Здесь же читается, что написал пользователь в
	 * консоли и отправляется на сервер
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		// создаем объект ClientReader'a, отдельного потока, который будет
		// читать сообщения с сервера и передавать нам
		// или на консоль
		final ClientReader clientReader = new ClientReader(socket);
		System.out.println("Type your name, please (or hit Enter to exit):");
		do {// тут цикл, ибо пользователь может не "попасть" на свободный ник ни
			// с первого, ни со второго... раза
			name = userInput.readLine(); // читаем предложенное пользователем в
											// консоли имя
			if (name.equals("")) { // если пользователь плюнул на попытки
									// зарегаться...
				close();
				System.exit(1); // выходим
			}
			writeString(OFFER_NAME + name); // комбинируем префикс команды и
											// имя, отправляя серверу
			String answer = clientReader.readString(); // ожидаем ответ от
														// сервера
			System.out.println(answer); // печатаем ответ в консоли
			if (answer.indexOf(REGISTER_SUCCESSFULL) != -1) { // если
																// регистрация
																// на сервере
																// успешна
				break; // ...выходим с цикла получения имени.
			}
		} while (true);
		// запускаем ClientReader'a в отдельном потоке.
		// Отныне он будет тупо ретранслировать полученное от сервера на консоль
		new Thread(clientReader, "ClientReader").start();

		String s;
		// в общем и в частности это главный цикл
		// "прочти пользователя - отправь серверу, если строка непустая"
		while (!(s = userInput.readLine()).equals("")) {
			writeString(s);
		}
		// а вот если пользователь просто нажал Enter, не введя текст, бум
		// считать, что он решил выйти с программы
		System.out.println("Bye-bye!");

	}

	/**
	 * Ну, main он main И есть. Входная точка.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Client c = null;
		try {
			// создаем клиента с параметрами по умолчанию, пробуем
			// приконнектиться.
			c = new Client("localhost", 45000);
			c.run(); // запускаем на общение клиента, если подключились удачно.
		} catch (IOException e) {
			// если сервера нет на хосте/порте, мы получим именно это сообщение
			if (e.getMessage().indexOf("Connection refused") == 0) {
				System.out
						.println("Unable to connect (Server is not running?)");
			} else if (!(e.getMessage().equalsIgnoreCase("Socket closed") || e
					.getMessage().equalsIgnoreCase("Connection reset"))) {
				// сюда попадем только если что-то было уж очень необычное. Не
				// закрытие сокета сервером или проблемы сети.
				e.printStackTrace();
			}
		} finally {
			if (c != null) {
				c.close(); // финальная попытка закрыть клиента. :)
			}
		}

	}
}