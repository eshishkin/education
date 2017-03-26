package com.eugene.education.chat;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Admin on 26.03.17.
 */
public class Message implements Serializable {
    private UUID id;
    private User from;
    private String content;
    private long time;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
