/*
* Собрал код дописанный в процессе выполнения домашки в один файл для удобства проверки 
*/

/*************************************** Файл AuthService.java ***********************************************************/
import java.sql.*;
/*
* В файлах чата имеется интерфейс, ответсвенный за аутентификацию, в оригинале реализованный
* как класс, хранящий список пользователей в ArrayList
* в оригинальный интерфейс добавил еще методы, позволяющие менять ник и добавлять пользователя
* сделал эти методы дефолтными, чтобы старая реализация не сломалась
*/
public interface AuthService {
	void start();
    String getNickByLoginPass(String login, String pass);
    default void changeNick(String oldNick, String newNick) {}
    default void addUser(String nick, String login, String pass) {}
	void stop();
}
/*
* Реализация интерфейса аутентификации с использованием SQLite
* Так как интерфейс может быть реализован разными способами
* пришлось обрабатывать все исключения SQLException внутри методов
* ведь переопределение интерфейсных методов не позволяет их пробрасывать
* в вызывающий код (во всяком случае Eclipse и IDEA ругались)
*/
class AdvancedAuthService implements AuthService {
	/* Запрос создания базы данных с пользователями
	CREATE TABLE users (
    nick     TEXT PRIMARY KEY,
    login    TEXT,
    password TEXT
	);
	*/
	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	private PreparedStatement ps;

    /*
    * при старте подключаемся к БД и готовим хранимый запрос
    */
    @Override
	public void start() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:users.db");
			ps = conn.prepareStatement("INSERT INTO users (nick, login, password) VALUES (?, ?, ?);");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /*
    * Тут у меня возникло затруднение, так как при выполнении
    * SELECT nick FROM users WHERE ...
    * у меня вылетало исключение и ResultSet не возвращался
    * пришлось делать SELECT * FROM users WHERE ...
    * и все заработало
    * Можете подсказать почему?
    */
    @Override
	public String getNickByLoginPass(String login, String pass) {
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM users WHERE login = '" + login + "' AND password = '" + pass + "';");
			if (rs.next())
				return rs.getString("nick");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

    /*
    * закрываем все, что открыли
    * лучше, конечно, каждый ресурс освобождать в свое блоке try/catch
    * но решил сэкономить в наборе кода )))
    */
    @Override
	public void stop() {
		try {
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
    
    /*
    * меняем ник
    * проверку на изменение своего ника, а не чужого,
    * мне кажется, должен делать класс ClientHandler, владеющий полной
    * информацией о клиенте, в конце-концов база данных
    * просто хранит набор значений и выполняет простые манипуляции
    * с этими наборами
    */
    @Override
    public void changeNick(String oldNick, String newNick) {
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE users SET nick = '" + newNick + "' WHERE nick = '" + oldNick + "';");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
    /*
    * метод добавления пользовател на будущее
    * пока в чате не предусмотрена возможность
    * регистрации, но пусть метод будет
    */
    @Override
    public void addUser(String nick, String login, String pass) {
		try {
			ps.setString(1, nick);
			ps.setString(2,  login);
			ps.setString(3, pass);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
/*********************************************** Файл MyServer.java ***********************************************/
/*
* Заменяем SimpleAuthService на AdvancedAuthService
*/
// import java.net.*;
// import java.io.*;
// import java.util.*;

public class MyServer {
	private final int PORT = 8189;
	private Vector<ClientHandler> clients;
	private AdvancedAuthService authService;
	
	public AuthService getAuthService() {
		return authService;
	}
	
	public MyServer() {
		try (ServerSocket serverSocket = new ServerSocket(PORT)){
			authService = new AdvancedAuthService();
			authService.start();
			clients = new Vector<>();
			while (true) {
				System.out.println("Server started, waiting for connection...");
				Socket socket = serverSocket.accept();
				System.out.println("Clinet is connected");
				new ClientHandler(this, socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (authService != null)
				authService.stop();
		}
    }
    // ......
}
/******************************************** Файл ClientHandler.java ********************************************/
/*
* В классе ClentHandler функции
*/
class ClientHandler {
    /*
    * Не требует изменений, т.к. интерфейс не поменялся
    */
    public void authentication() throws IOException {
		while (true) {
			String str = in.readUTF();
			if (str.startsWith("/auth")) {
				String[] tokens = str.split("\\s");
				String nick = server.getAuthService().getNickByLoginPass(tokens[1], tokens[2]);
				if (nick != null) {
					if (!server.isNickBusy(nick)) {
						sendMessage("/authok" + nick);
						name = nick;
						server.broadcastMessage(name + " joined the chat");
						server.subscribe(this);
						return;
					} else {
						sendMessage("This nickname is aleady used");
					}
				} else {
					sendMessage("Wrong login/password");
				}
			}
		}
    }
    /*
    * Добавляем обработчик для сообщения "/chnick oldNick newNick" - изменить ник
    */
    public void readMessage() throws IOException {
		while (true) {
			String msgFromClient = in.readUTF();
			System.out.println("from " + name + ": " + msgFromClient);
			if (msgFromClient.equals("/end")) {
				return;
            }
            if (msgFromClient.startsWith("/chnick")) {
                String[] tokens = msgFromClient.split("\\s");
                // проверяем свой ли ник пытается поменять пользователь
                if (this.name.equals(tokens[1]))
                    server.getAuthService().changeNick(tokens[1], tokens[2]);
                else
                    sendMessage("Пользователь имеет право изменять только свой ник");
            }
			server.broadcastMessage(name + ": " + msgFromClient);
		}
	}
}