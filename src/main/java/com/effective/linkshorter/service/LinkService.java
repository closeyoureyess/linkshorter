package com.effective.linkshorter.service;

import com.effective.linkshorter.entity.Link;
import com.effective.linkshorter.entity.LinkDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface LinkService {
    Optional<LinkDto> linkCutter(LinkDto linkDto, HttpServletRequest request);

    Optional<LinkDto> getSourceLink(LinkDto linkDto);
}
