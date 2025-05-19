package com.effective.linkshorter.service;

import com.effective.linkshorter.entity.Link;
import com.effective.linkshorter.entity.LinkDto;
import com.effective.linkshorter.mapper.LinkMapper;
import com.effective.linkshorter.repository.LinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

import static com.effective.linkshorter.others.ConstantsClass.CHARS;
import static com.effective.linkshorter.others.ConstantsClass.CHARS_LENGTH;

@Service
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
    @Transactional
    @Override
    public Optional<LinkDto> linkCutter(LinkDto linkDto, HttpServletRequest request) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        String sourceLink = linkDto.getLink();
        int linkLength = sourceLink.length();

        for (int i = 0; i < linkLength / 2; i++) {
            int randomInt = secureRandom.nextInt(CHARS_LENGTH);
            stringBuilder.append(CHARS.charAt(randomInt));
            if (i == (linkLength / 2) - 1) {
                Optional<Object> optionalObject =
                        cacheService.getFromCache("linkServiceCache", stringBuilder.toString());
                linkDto = existValueCacheOrDB(optionalObject, linkDto, stringBuilder);
                if (linkDto != null && !sourceLink.equals(linkDto.getLink())) {
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
            linkDto = linkMapper.convertLinkToDto(
                    linkRepository.save(
                            linkMapper.convertDtoToLink(linkDto)
                    )
            );
        }
        return Optional.ofNullable(linkDto);
    }

    private LinkDto existValueCacheOrDB(Optional<Object> optionalObject, LinkDto linkDto,
                                        StringBuilder stringBuilder) {
        if (optionalObject.isPresent()) {
            linkDto = (LinkDto) optionalObject.get();
        } else {
            Optional<Link> optionalLink =
                    linkRepository.findByUniqueIdentifierAndSessionId(stringBuilder.toString());
            if (optionalLink.isPresent()) {
                linkDto = linkMapper.convertLinkToDto(optionalLink.get());
            }
        }
        return linkDto;
    }

    private String getSessionId(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        return httpSession.getId();
    }

    @Cacheable(cacheNames = "linkServiceCache", key = "#result.uniqueIdentifier")
    @Transactional
    @Override
    public Optional<LinkDto> getSourceLink(LinkDto linkDto) {
        String uniqueIdentifier = linkDto.getUniqueIdentifier();
        Optional<Object> optionalObject =
                cacheService.getFromCache("linkServiceCache", uniqueIdentifier);
        return null;
    }
}
