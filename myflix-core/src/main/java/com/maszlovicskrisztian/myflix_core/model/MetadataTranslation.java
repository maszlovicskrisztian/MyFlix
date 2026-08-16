package com.maszlovicskrisztian.myflix_core.model;

import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "metadata_translation",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"entity_type", "entity_id", "language_code"}))
@Getter
@Setter
@NoArgsConstructor
public class MetadataTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MediaType entityType;

    private Long entityId;
    private String languageCode;
    private String title;

    @Lob
    private String overview;
}
