package ru.kaulina;

import ru.kaulina.thread.ThreadReadMessage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String userName = "Аноним";

    public static String getUserName() {
        return userName;
    }


    public static void setUserName(String userName) {
        if (!userName.trim().isEmpty())
            Client.userName = userName;
    }


    public static void main(String[] args) {
        String serverHost = null;
        int serverPort = 0;

        try (BufferedReader bf = new BufferedReader(new FileReader("src/main/resources/settings.txt"))) {
            String setting;
            while ((setting = bf.readLine()) != null) {
                if (setting.contains("host")) {
                    String[] s = setting.split(" ");
                    serverHost = s[1];
                } else if (setting.contains("port")) {
                    String[] s = setting.split(" ");
                    serverPort = Integer.parseInt(s[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Socket socketClient = new Socket(serverHost, serverPort);
             PrintWriter printWriter = new PrintWriter(socketClient.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()))) {
            System.out.println("The client connected to the socketclient");

            Scanner scanner = new Scanner(System.in);
            System.out.println("\nEnter your name to join the server!\n" +
                    "Then enter messages to send to users or 'exit' to exit the channel: CLIENT");

            setUserName(scanner.nextLine());
            printWriter.println(getUserName());

            Thread.sleep(1000);
            //Поток для чтения новых сообщений
            ThreadReadMessage send = new ThreadReadMessage();
            send.start();

            while (true) {
                // ждём консоли клиента на предмет появления в ней данных
                String msg = scanner.nextLine();
                printWriter.println(msg);
                if (msg.equalsIgnoreCase("exit")) {
                    Thread.sleep(100);
                    if (in.read() > -1) {
                        msgFromServer(in);
                    }
                    break;
                }
                if (in.read() > -1) {
                    msgFromServer(in);
                }
            }
            send.interrupt();
            System.out.println("Closing the connection channel - DONE.");
        } catch (IOException | InterruptedException e) {
            return;
        }
    }

    private static void msgFromServer(BufferedReader in) throws IOException {
        String msgServ = in.readLine();
    }
}