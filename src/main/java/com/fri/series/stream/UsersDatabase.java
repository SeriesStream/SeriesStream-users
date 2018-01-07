package com.fri.series.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class UsersDatabase {
    static User u1 = new User(1,"marko","test");
    static User u2 = new User(2,"drugi","dve");
    static User u3 = new User(3,"nekdo","nekdo");
    private static List<User> users = Arrays.asList(u1,u2,u3);

    public static List<User> getUsers() {
        System.out.println("List getted"); return users;
    }

    public static User getUser(int id) {
        for (User user : users) {
            if (user.getId() == (id))
                return user;
        }

        return null;
    }

    public static void addUser(User user) {
        users.add(user);
    }

    public static void deleteUser(int id) {
        for (User user : users) {
            if (user.getId() == (id)){
                users.remove(user);
                break;
            }
        }
    }
}
