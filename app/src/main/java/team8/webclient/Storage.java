package team8.webclient;

import java.util.ArrayList;

//Klasse für das Zwischenspeichern von verschiedenen Sachen
public class Storage {
    private static String username;
    private static String password;
    private static String pubkey;
    private static byte[] pubkey1;
    private static byte[] privkey;
    private static byte[] salt;

    private static ArrayList<String> nachrichten = new ArrayList<String>();

    public static void setNachrichten(String nachricht){
        nachrichten.add(nachricht);
    }

    public static ArrayList<String> getNachrichten(){
        return nachrichten;
    }

    public static void deleteNachrichten(){
        nachrichten.clear();
    }

    public static void setUsername(String usernameInput){
        username = usernameInput;
    }

    public static void setPassword(String passwordInput){
        password = passwordInput;
    }

    public static String getUsername(){
        return username;
    }

    public static String getPassword(){
        return password;
    }

    public static void setPrivkey(byte[] privkeyInput){
        privkey = privkeyInput;
    }

    public static byte[] getPrivkey(){
        return privkey;
    }

    public static void setPubkey(String pubkeyInput){
        pubkey = pubkeyInput;
    }
}
