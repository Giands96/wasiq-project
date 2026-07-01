package com.wasiq.inmobiliaria.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wasiq.inmobiliaria.property.model.Property;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "media")
@Builder
public class Media {
    //Atributos para almacenar las imagenes de las proppiedades
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY
    )
    private Long id;
    @Column(nullable = false, name = "url")
    private String url;
    @Column(nullable = false, name = "type")
    private String type; // e.g., "image/jpeg", "video/mp4"
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "property_id", nullable = false)
    private Property property  ; // ID de la propiedad a la que pertenece el medio

}
