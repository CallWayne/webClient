package team8.webclient;

/**
 * Created by niklasschluter on 20.06.16.
 */
public class Storage {
    private static String username;
    private static String password;
    private static byte[] privkey;

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
}
