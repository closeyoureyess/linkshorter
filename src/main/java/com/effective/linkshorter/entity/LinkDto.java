package com.effective.linkshorter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LinkDto {

    private Long id;

    private String link;

    private String uniqueIdentifier;

    private String linkWithUniqueIdentifier;
}
