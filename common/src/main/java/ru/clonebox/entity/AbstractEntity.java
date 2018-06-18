package ru.clonebox.entity;

import javax.persistence.*;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
