package ru.kaulina;

import ru.kaulina.logger.ServerLog;
import ru.kaulina.thread.ThreadClientHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final Integer PORT = 7879;
    public static final String HOST = "localhost\n";
    public static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static ServerLog LOGGER = ServerLog.getInstance();

    public Server(Integer port) {
    }

    public static void main(String[] args) {
        try (FileWriter fileWriter = new FileWriter("src//main//resources//settings.txt", false)) {
            fileWriter.write("host: " + HOST);
            fileWriter.write("port: " + String.valueOf(PORT));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             Scanner sc = new Scanner(System.in)) {

            LOGGER.log("INFO", "Hello! Server is active," +
                    " Waiting for console commands or user connection.." +
                    " \nTo shut down the server - write \"exit\"");

            Thread readThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Thread.sleep(1000);
                        if (sc.hasNextLine()) {
                            //  System.out.println("Сервер нашёл команды!");
                            System.out.println("The server found the commands!");
                            Thread.sleep(1000);
                            String serverCommand = sc.nextLine();
                            if (serverCommand.equalsIgnoreCase("exit")) {
                                // System.out.println("Сервер инициализирует выход");
                                System.out.println("Server initializes output");
                                executorService.shutdown();
                                serverSocket.close();
                                LOGGER.log("INFO", "EXIT");
                                break;
                            }
                        }
                    } catch (InterruptedException | IOException e) {
                        return;
                    }
                }
            });
            readThread.start();

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                // System.out.println("Есть подключение");
                System.out.println("There is a connection");
                // после получения запроса на подключение сервер создаёт сокет
                // для общения с клиентом и отправляет его в отдельный поток
                // - ThreadClientHandler и тот продолжает общение от лица сервера
                executorService.execute(new ThreadClientHandler(clientSocket, LOGGER));
                // System.out.println("Подключение установлено");
                System.out.println("Connection ready");
            }
            // System.out.println("Пытаемся выйти");
            System.out.println("We're trying to get out");
            executorService.shutdown();
        } catch (IOException e) {
            return;
        }
    }
}