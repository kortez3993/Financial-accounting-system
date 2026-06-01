package backendjava.dto;

import jakarta.validation.constraints.NotBlank;

public class RegistrationRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String name;

    public RegistrationRequest() {
    }

    public RegistrationRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }
}
