package backendjava.dto;

import java.time.OffsetDateTime;

public class CategoryResponse {
    private Long id;
    private String name;
    private String type;
    private String color;
    private OffsetDateTime createdAt;

    public CategoryResponse(Long id, String name, String type, String color, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
