package com.effective.linkshorter.mapper;

import com.effective.linkshorter.entity.Link;
import com.effective.linkshorter.entity.LinkDto;
import org.springframework.stereotype.Component;

@Component
public class LinkMapperImpl implements LinkMapper {
    @Override
    public Link convertDtoToLink(LinkDto linkDto) {
        Link link = new Link();
        link.setId(linkDto.getId());
        link.setLink(linkDto.getLink());
        link.setUniqueIdentifier(linkDto.getUniqueIdentifier());
        return link;
    }

    @Override
    public LinkDto convertLinkToDto(Link link) {
        LinkDto linkDto = new LinkDto();
        linkDto.setId(link.getId());
        linkDto.setLink(link.getLink());
        linkDto.setUniqueIdentifier(link.getUniqueIdentifier());
        return linkDto;
    }
}
