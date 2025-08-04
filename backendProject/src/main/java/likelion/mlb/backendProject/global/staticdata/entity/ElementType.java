package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "element_type")
@Getter
@Setter
public class ElementType {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "kr_name", nullable = false)
    private String krName;

    @Column(name = "singular_name", nullable = false)
    private String singularName;
}