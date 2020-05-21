/*
* Так как запись лог-файла происходит на стороне клиента,
* изменения внесены только в файл ClentGUI.java
* добавлены 2 функции readFromLog() и writeToLog(String s)
* (1) readFromLog() вызывается только один раз в конструкторе для заполнения 
* текстовой области чата (chatArea) последними <=100 сообщениями
* (2) writeToLog(String s) вызывается каждый раз при обнавлении 
* текстовой области чата (когда приходит сообщение с сервера и когда отсылается сообщение),
* собирает в буфер 10 сообщений и потом пишет в лог-файл
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientGUI extends JFrame implements ActionListener {
    private final int WIDTH = 400;
    private final int HEIGHT = 500;
    // компоненты пользовательского интерфейса
    // панель для подключения к серверу
    private JPanel panelTop;
    private JTextField loginField;
    private JPasswordField passField;
    private JButton btnConnect;
    // область чата и списка пользователей
    private JTextArea chatArea;
    private JList<String> userList;
    // панель для ввода и отсылки сообщений
    private JPanel panelBottom;
    private JTextField messageField;
    private JButton btnSend;
    // сокет и потоки для общения с сервером
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    // потоки ввода-вывода для сохранения лога
    private ArrayList<String> buffer;

    public ClientGUI() {
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // заполняем верхнюю панель
        panelTop = new JPanel(new GridLayout(1, 3));
        loginField = new JTextField("Login");
        passField = new JPasswordField("Password");
        btnConnect = new JButton("Connect");
        btnConnect.addActionListener(this);
        panelTop.add(loginField);
        panelTop.add(passField);
        panelTop.add(btnConnect);
        // заполняем область вывода сообщений и списка пользователей
        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setEditable(false);
        // получаем 100 строк и затем заполняем ими chatArea
        buffer = new ArrayList<>();
        readFromLog();
        JScrollPane scrChat = new JScrollPane(chatArea);
        userList = new JList<>();
        userList.setListData(new String[] {"user1", "user2", "user3", "user4", "user5"});
        JScrollPane scrUsers = new JScrollPane(userList);
        scrUsers.setPreferredSize(new Dimension(100, 0));
        // заполняем нижнюю панель
        panelBottom = new JPanel(new BorderLayout());
        messageField = new JTextField();
        btnSend = new JButton("Send");
        btnSend.addActionListener(this);
        panelBottom.add(messageField, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);
        add(scrChat, BorderLayout.CENTER);
        add(scrUsers, BorderLayout.EAST);
        add(panelBottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    // читаем файл, оставляем 100 строк и затем заполняем ими chatArea
    private void readFromLog() {
        ArrayList<String> buff = new ArrayList<>();
        File file = new File("log.txt");
        if (!file.exists()) {
            return;
        }
        try (Scanner fin = new Scanner(new FileInputStream(file), "UTF-8")) {
            while(fin.hasNext())
                buff.add(fin.nextLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int lineCount = buff.size();
        int offset = lineCount > 100 ? lineCount - 100 : 0;
        for (int i = offset; i < lineCount; i++) {
            chatArea.append(buff.get(i) + System.lineSeparator());
        }
    }

    // сохраняем сообщения в буфер, если в буфере более 10 сообщений,
    // пишем их в лог (уменьшаем количество обращений к диску, хотя можем потерять
    // 10 последних сообщений в случае падения программы)
    // в целом, функция вызывается после каждого обновления chatArea (кроме первичного заполнения в конструкторе)
    private void writeToLog(String msg) {
        buffer.add(msg);
        if (buffer.size() > 10) {
            try(PrintWriter fout = new PrintWriter(new FileOutputStream("log.txt", true), true)) {
                for (String s : buffer)
                    fout.println(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnConnect) {
            connect();
        } else if (source == btnSend || source == messageField) {
            sendMessage();
        } else {
            throw new RuntimeException("Unknown source: " + source);
        }
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            String authMsg = "/auth" + loginField.getText() + " " + new String(passField.getPassword());
            out.writeUTF(authMsg);
            loginField.setText("");
            passField.setText("");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String msgFromServer = in.readUTF();
                            if (msgFromServer.startsWith("/authok")) {
                                break;
                            }
                            chatArea.append(msgFromServer + "\n");
                            // пишем в лог-файл
                            writeToLog(msgFromServer);
                        }
                        while (true) {
                            String msgFromServer = in.readUTF();
                            if (msgFromServer.equalsIgnoreCase("/end")) {
                                break;
                            }
                            chatArea.append(msgFromServer + "\n");
                            // пишем в лог-файл
                            writeToLog(msgFromServer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String msg = messageField.getText();
        String userName = loginField.getText();
        if ("".equals(msg)) return;
        messageField.setText("");
        messageField.requestFocusInWindow();
        chatArea.append(msg + System.lineSeparator());
        // пишем в лог-файл
        writeToLog(msg);
        try {
            out.writeUTF(String.format("%s: %s", userName, msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
           @Override
           public void run() {
               new ClientGUI();
           }
        });
    }
}
