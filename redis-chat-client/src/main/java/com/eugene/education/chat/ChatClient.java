package com.eugene.education.chat;

import org.apache.commons.lang3.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Admin on 26.03.17.
 */
public class ChatClient {

    private static final String MESSAGE_KEY_PREFIX = "message::";
    private static final String USER_KEY_PREFIX = "user::";

    private static final String CHAT_CHANNEL = "chat-channel";
    private static final String USER_CHANNEL = "user-channel";

    private Jedis commonConnection;
    private Jedis publisherConnection;
    private Jedis subscriberConnection;
    private User user;
    private MessageListener listener;

    public ChatClient(String host, User user, MessageListener listener) {
        commonConnection = new Jedis(host);
        publisherConnection = new Jedis(host);
        subscriberConnection = new Jedis(host);
        this.user = user;
        this.listener = listener;
    }

    public String getServerInfo() {
        return commonConnection.info();
    }

    public void registerUser() {
        String key = USER_KEY_PREFIX + user.getId();
        String status = commonConnection.set(toBytes(key), toBytes(user));
        System.out.println("Trying to add a user:"  + status);
    }

    public void subscribe() {
        publisherConnection.publish(USER_CHANNEL,
                String.format("User %s has been subscribed for the chat as %s (%s)",
                        user.getId(), user.getName(), user.getNickName()));

        subscriberConnection.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String m) {
                super.onMessage(channel, m);
                if (USER_CHANNEL.equals(channel)) {
                    System.out.println(m);
                    return;
                }

                byte[] bytes = commonConnection.get(toBytes(m));
                if (bytes == null) {
                    throw new RuntimeException("Could not find a message by incoming id: " + m);
                }
                listener.onMessage(SerializationUtils.deserialize(bytes));
            }
        }, CHAT_CHANNEL, USER_CHANNEL);

    }

    public void close() {
        publisherConnection.publish(USER_CHANNEL,
                String.format("%s has gone", user.getId()));

        subscriberConnection.getClient().unsubscribe();
        commonConnection.getClient().unsubscribe();
        publisherConnection.getClient().unsubscribe();
    }

    public void send(String content) {
        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setTime(new Date().getTime());
        message.setFrom(user);
        message.setContent(content);

        String key = MESSAGE_KEY_PREFIX  + message.getId();

        commonConnection.set(toBytes(key), toBytes(message));
        publisherConnection.publish(CHAT_CHANNEL, key);
    }

    public static interface MessageListener {
        void onMessage(Message message);
    }

    private byte[] toBytes(Serializable o) {
        return SerializationUtils.serialize(o);
    }
}
