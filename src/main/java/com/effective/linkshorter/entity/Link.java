package com.effective.linkshorter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "link")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_gen")
    @SequenceGenerator(name = "link_gen", sequenceName = "my_entity_sequence", allocationSize = 5)
    private Long id;

    @Column(name = "source_link")
    private String link;

    @Column(name = "unique_identifier")
    private String uniqueIdentifier;

    @Column(name = "link_unique_identifier")
    private String linkWithUniqueIdentifier;

    @Column(name = "createDate")
    private Instant createDate;

    @Column(name = "expireDate")
    private Instant expireDate;

}
