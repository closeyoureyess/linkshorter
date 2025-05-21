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
        link.setSourceLink(linkDto.getSourceLink());
        link.setUniqueIdentifier(linkDto.getUniqueIdentifier());
        link.setLinkWithUniqueIdentifier(linkDto.getLinkWithUniqueIdentifier());
        return link;
    }

    @Override
    public LinkDto convertLinkToDto(Link link) {
        LinkDto linkDto = new LinkDto();
        linkDto.setId(link.getId());
        linkDto.setSourceLink(link.getSourceLink());
        linkDto.setUniqueIdentifier(link.getUniqueIdentifier());
        linkDto.setLinkWithUniqueIdentifier(link.getLinkWithUniqueIdentifier());
        return linkDto;
    }
}
