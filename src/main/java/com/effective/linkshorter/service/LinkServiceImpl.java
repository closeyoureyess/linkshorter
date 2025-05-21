package com.effective.linkshorter.service;

import com.effective.linkshorter.entity.Link;
import com.effective.linkshorter.entity.LinkDto;
import com.effective.linkshorter.mapper.LinkMapper;
import com.effective.linkshorter.repository.LinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

import static com.effective.linkshorter.others.ConstantsClass.CHARS;
import static com.effective.linkshorter.others.ConstantsClass.CHARS_LENGTH;

@Service
@Slf4j
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;

    private final LinkMapper linkMapper;

    private final CacheService cacheService;

    @Autowired
    public LinkServiceImpl(LinkRepository linkRepository, LinkMapper linkMapper, CacheService cacheService) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.cacheService = cacheService;
    }

    @CachePut(cacheNames = "linkServiceCache", key = "#result.uniqueIdentifier")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    @Override
    public Optional<LinkDto> linkCutter(LinkDto linkDto, HttpServletRequest request) {
        wipeRedundantFields(linkDto);
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        String sourceLink = linkDto.getSourceLink();
        int linkLength = sourceLink.length();
        for (int i = 0; i < linkLength / 2; i++) {
            int randomInt = secureRandom.nextInt(CHARS_LENGTH);
            stringBuilder.append(CHARS.charAt(randomInt));
            if (i == (linkLength / 2) - 1) {
                Optional<Object> optionalObject =
                        cacheService.getFromCache("linkServiceCache", stringBuilder.toString());
                linkDto = existValueCacheOrDB(optionalObject, linkDto, stringBuilder);
                if (linkDto != null && !sourceLink.equals(linkDto.getSourceLink())) {
                    i = 0;
                    stringBuilder.setLength(0);
                }
            }
        }
        String uniqueIdentifier = stringBuilder.toString();
        if (linkDto != null && linkDto.getUniqueIdentifier() == null) {
            linkDto.setUniqueIdentifier(uniqueIdentifier);
            uniqueIdentifier = stringBuilder.insert(0, "http://localhost:8080/").toString();
            linkDto.setLinkWithUniqueIdentifier(uniqueIdentifier);
            Link linkForSaveDB = linkMapper.convertDtoToLink(linkDto);
            linkForSaveDB = linkRepository.save(linkForSaveDB);
            linkDto = linkMapper.convertLinkToDto(linkForSaveDB);
        }
        return Optional.ofNullable(linkDto);
    }

    private LinkDto existValueCacheOrDB(Optional<Object> optionalObject, LinkDto linkDto,
                                        StringBuilder stringBuilder) {
        if (optionalObject.isPresent()) {
            linkDto = (LinkDto) optionalObject.get();
        } else {
            Optional<Link> optionalLink = linkRepository.findByUniqueIdentifier(stringBuilder.toString());
            if (optionalLink.isPresent()) {
                linkDto = linkMapper.convertLinkToDto(optionalLink.get());
            }
        }
        return linkDto;
    }

    private void wipeRedundantFields(LinkDto linkDto) {
        linkDto.setId(null);
        linkDto.setLinkWithUniqueIdentifier(null);
        linkDto.setUniqueIdentifier(null);
    }

    @Cacheable(cacheNames = "linkServiceCache", key = "#uniqueIdentifier")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    @Override
    public Optional<LinkDto> getSourceLink(String uniqueIdentifier) {
        Optional<Link> optionalLink = linkRepository.findByUniqueIdentifier(uniqueIdentifier);
        LinkDto linkDto = null;
        if (optionalLink.isPresent()) {
            linkDto = linkMapper.convertLinkToDto(optionalLink.get());
        }
        return Optional.ofNullable(linkDto);
    }
}
