package com.effective.linkshorter.entity;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonFilter("LinkDtoFilter")
public class LinkDto implements Serializable {

    private Long id;

    private String sourceLink;

    private String uniqueIdentifier;

    private String linkWithUniqueIdentifier;
}
