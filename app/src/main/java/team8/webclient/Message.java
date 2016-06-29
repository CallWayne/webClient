package team8.webclient;

//Klasse für das zwischenspeichern von den entschlüsselten Nachrichten
public class Message {

    byte[] sender;
    byte[] content_enc;
    byte[] iv;
    byte[] key_recipient_enc;
    byte[] sig_recipient;
    byte[] created_at;

    public Message(byte[] sender, byte[] content_enc, byte[] iv, byte[] key_recipient_enc, byte[] sig_recipient, byte[] created_at){
        this.sender = sender;
        this.content_enc = content_enc;
        this.iv = iv;
        this.key_recipient_enc = key_recipient_enc;
        this.sig_recipient = sig_recipient;
        this.created_at = created_at;
    }

    public byte[] getSender(){
        return sender;
    }

    public byte[] getContent_enc(){
        return content_enc;
    }

    public byte[] getIv(){
        return iv;
    }

    public byte[] getKey_recipient_enc(){
        return key_recipient_enc;
    }

    public byte[] getSig_recipient(){
        return sig_recipient;
    }

    public byte[] getCreated_at(){
        return created_at;
    }
}
