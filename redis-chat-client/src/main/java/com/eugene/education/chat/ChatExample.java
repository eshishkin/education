package com.eugene.education.chat;

import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by Admin on 26.03.17.
 */
public class ChatExample {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd.MM.yy hh.mm.ss");

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.out.println("Required parameters are missing.");
            System.out.println("use java -jar <jar-name> <redis-host>");
            return;
        }
        String host = args[0];

        System.out.println("Hi user, please name yourself. " +
                "Just print your email, name and nickname separated by comma");

        Scanner scanner = new Scanner(System.in);
        User user = initializeUser(scanner.nextLine().split(","));

        ChatClient client = new ChatClient(host, user, message -> {
            User u = message.getFrom();
            String time = FORMATTER.format(new Date(message.getTime()));
            System.out.println(String.format("[%s] %s (%s) wrote: %s",
                    time, u.getName(), u.getNickName(), message.getContent()
            ));
        });

        System.out.println("Server's info is:" + client.getServerInfo());
        System.out.println("Successfully connected to the server");

        client.registerUser();

        System.out.println("--------------------------------------------");
        System.out.println();

        Thread thread = new Thread(client::subscribe);
        thread.start();

        while (true) {
            String msg = scanner.nextLine();
            if ("".equals(msg.trim())) continue;
            if (":quit".equals(msg)) {
                client.close();
                thread.join();
                break;
            }
            client.send(msg);
        }
        System.out.println("Terminating");
    }

    private static User initializeUser(String[] data) {
        if (data.length != 3) {
            throw new RuntimeException("Could not recognize a user." +
                    " Make sure you entered your name in right format:" + data);
        }

        return new User(data[0], data[1], data[2]);
    }
}
