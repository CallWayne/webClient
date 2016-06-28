package team8.webclient;

/**
 * Created by niklasschluter on 20.06.16.
 */
public class Storage {
    private static String username;
    private static String password;
    private static byte[] privkey;
    private static byte[] salt;


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

    public static void setSalt(byte[] saltInput){
        salt = saltInput;
    }

    public static byte[] getSalt(){
        return salt;
    }

    public static void setPrivkey(byte[] privkeyInput){
        salt = privkeyInput;
    }

    public static byte[] getPrivkey(){
        return privkey;
    }
}
