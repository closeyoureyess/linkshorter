package com.effective.linkshorter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "source_link")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_gen")
    @SequenceGenerator(name = "link_gen", sequenceName = "my_entity_sequence", allocationSize = 5)
    private Long id;

    @Column(name = "base_source_link")
    private String sourceLink;

    @Column(name = "unique_identifier")
    private String uniqueIdentifier;

    @Column(name = "link_unique_identifier")
    private String linkWithUniqueIdentifier;

}
