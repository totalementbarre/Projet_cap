package database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserInfos {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String username;
    private String hashedPassword;
    private String badgeId;
    private String encryptedRetina;
    private int selX;
    private int selY;

    public UserInfos() {
    }

    public UserInfos(String username, String hashedPassword, String badgeId, String encryptedRetina, int selX, int selY) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.badgeId = badgeId;
        this.encryptedRetina = encryptedRetina;
        this.selX = selX;
        this.selY = selY;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public String getEncryptedRetina() {
        return encryptedRetina;
    }

    public int getSelX() {
        return selX;
    }

    public int getSelY() {
        return selY;
    }
}
