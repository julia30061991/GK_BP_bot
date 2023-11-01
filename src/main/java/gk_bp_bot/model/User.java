package gk_bp_bot.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@NoArgsConstructor
@Entity
@Table(name = "tg_user")
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "INT")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(name = "chat_user_id", columnDefinition = "LONG")
    private Long chatId;

    @Column(name = "user_first_name", columnDefinition = "VARCHAR(255)")
    private String firstName;

    @Column(name = "user_last_name", columnDefinition = "VARCHAR(255)")
    private String lastName;

    @Column(name = "user_tg_name", columnDefinition = "VARCHAR(255)")
    private String userName;

    @Column(name = "reg_time", columnDefinition = "DATETIME")
    private Timestamp reg_time;

    public User (Long chatId, String firstName, String lastName, String userName, Timestamp time_reg) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.reg_time = time_reg;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getReg_time() {
        return reg_time;
    }

    public void setReg_time(Timestamp reg_time) {
        this.reg_time = reg_time;
    }

    @Override
    public String toString() {
        return "Пользователь с id " + chatId + ", полное имя: " + firstName + " "
                + lastName + ", короткое имя " + userName + ", дата регистрации: " + reg_time;
    }
}