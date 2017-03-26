package com.eugene.education.chat;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Admin on 26.03.17.
 */
public class User implements Serializable {
    private String id;
    private String name;
    private String nickName;

    public User(String id, String name, String nickName) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return String.format("[id=%s, name=%s, nick=%s]", id, name, nickName);
    }
}
