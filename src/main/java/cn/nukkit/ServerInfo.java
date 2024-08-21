package cn.nukkit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

public class ServerInfo<E> extends HashSet<E> {
    public static ServerInfo<String> banByName = new ServerInfo<>();

    public static ServerInfo<String> banByIP = new ServerInfo<>();

    public static ServerInfo<String> operators = new ServerInfo<>();

    public static ServerInfo<String> whitelist = new ServerInfo<>();

    public String infoName;
    public static HashMap<String, Field> fields = new HashMap<>();

    @Override
    public boolean add(E e) {
        boolean f = super.add(e);
        Server.getInstance().saveInfo(infoName, true);
        return f;
    }

    @Override
    public boolean remove(Object e) {
        boolean f = super.remove(e);
        Server.getInstance().saveInfo(infoName, true);
        return f;
    }
}
