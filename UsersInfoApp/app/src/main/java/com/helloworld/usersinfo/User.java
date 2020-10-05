package com.helloworld.usersinfo;

import java.io.Serializable;

public class User implements Serializable {
    String fname, lname, email, id, gender;
    String age;

    @Override
    public String toString() {
        return "User{" +
                "fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                '}';
    }
}
