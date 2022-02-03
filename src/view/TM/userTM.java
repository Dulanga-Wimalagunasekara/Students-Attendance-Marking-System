package view.TM;


import javafx.scene.control.Button;

public class userTM {
    private String username;
    private String name;
    private String password;
    private String role;
    private Button removeButton;

    public userTM(String username, String name, String password, String role, Button removeButton) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.role = role;
        this.removeButton = removeButton;
    }

    public userTM() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public void setRemoveButton(Button removeButton) {
        this.removeButton = removeButton;
    }

    @Override
    public String toString() {
        return "userTM{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", removeButton=" + removeButton +
                '}';
    }
}
