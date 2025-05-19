package com.effective.linkshorter.mapper;

import com.effective.linkshorter.entity.Link;
import com.effective.linkshorter.entity.LinkDto;
import jakarta.validation.constraints.NotNull;

public interface LinkMapper {

    Link convertDtoToLink(@NotNull LinkDto linkDto);

    LinkDto convertLinkToDto(@NotNull Link link);
}
