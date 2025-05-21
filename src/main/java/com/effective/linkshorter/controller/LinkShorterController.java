package com.effective.linkshorter.controller;

import com.effective.linkshorter.annotations.FilterResponse;
import com.effective.linkshorter.entity.LinkDto;
import com.effective.linkshorter.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.effective.linkshorter.others.ConstantsClass.POST_CREATE_LINK;

@RestController
@RequestMapping
@Validated
public class LinkShorterController {

    private final LinkService linkService;

    @Autowired
    LinkShorterController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping("/sourceLink")
    @FilterResponse(filterName = POST_CREATE_LINK)
    ResponseEntity<LinkDto> linkCutter(@Valid @NotNull @RequestBody LinkDto linkDto, HttpServletRequest request) {
        Optional<LinkDto> optionalLinkDto = linkService.linkCutter(linkDto, request);
        return optionalLinkDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/{uniqueIdentifier}")
    ResponseEntity<String> redirectOriginalLink(@NotBlank @PathVariable String uniqueIdentifier) {
        Optional<LinkDto> optionalLinkDto = linkService.getSourceLink(uniqueIdentifier);
        if (optionalLinkDto.isPresent()) {
            LinkDto linkDto = optionalLinkDto.get();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, linkDto.getSourceLink())
                    .build();
        }
        return ResponseEntity.notFound().build();
    }
}
