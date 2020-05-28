/**
 * 2. На серверной стороне сетевого чата реализовать управление потоками через ExecutorService.
*/
// Для этого - преобразуем ClientHandler как реализацию интерфейса Runnable
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class ClientHandler implements Runnable {
	private MyServer server;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String name;

	public String getName() {
		return name;
	}

	public ClientHandler(MyServer server, Socket socket) {
		try {
			this.server = server;
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.name = "";
		} catch (IOException e) {
			throw new RuntimeException("Problems creating Client Handler");
		}
	}

    // переопределяем метод run
    @Override
	public void run() {
		try {
			authentication();
			readMessage();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}

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

	public void readMessage() throws IOException {
		while (true) {
			String msgFromClient = in.readUTF();
			System.out.println("from " + name + ": " + msgFromClient);
			if (msgFromClient.equals("/end")) {
				return;
			}
			server.broadcastMessage(name + ": " + msgFromClient);
		}
	}

	public void sendMessage(String msg) {
		try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		server.unsubscribe(this);
		server.broadcastMessage(name + " leaving the chat");
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
// В класс MyServer добавляем поле, хранящее ExecutorService
public class MyServer {
	private final int PORT = 8189;
	private Vector<ClientHandler> clients;
	private AuthService authService;
	private ExecutorService executor; // <---------
	
	public AuthService getAuthService() {
		return authService;
	}
	
	public MyServer() {
		try (ServerSocket serverSocket = new ServerSocket(PORT)){
			authService = new SimpleAuthService();
			authService.start();
            clients = new Vector<>();
            // инициализируем ExecutorService новым объектом типа CachedThreadPool
            // т.к. неизвестно сколько клиентов одновременно могут подключиться
			executor = Executors.newCachedThreadPool(); // <---------
			while (true) {
				System.out.println("Server started, waiting for connection...");
				Socket socket = serverSocket.accept();
                System.out.println("Clinet is connected");
                // создаем нового клиента при подключении
                ClientHandler client =  new ClientHandler(this, socket);
                // добавляем поток с новым клиентом в пул
				executor.execute(new Thread(client));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (authService != null)
                authService.stop();
                // при остановке сервера, выключаем ExecutorService
                executor.shutdown();
		}
	}
	
	public synchronized boolean isNickBusy(String nick) {
		for (ClientHandler client : clients) {
			if (client.getName().equals(nick))
				return true;
		}
		return false;
	}
	
	public synchronized void broadcastMessage(String msg) {
		for (ClientHandler client : clients)
			client.sendMessage(msg);
	}
	
	public synchronized void subscribe(ClientHandler client) {
		clients.add(client);
	}
	
	public synchronized void unsubscribe(ClientHandler client) {
		clients.remove(client);
    }
    
    public static void main(String[] args) {
		new MyServer();
	}
}
