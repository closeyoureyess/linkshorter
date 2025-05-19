package com.effective.linkshorter.controller;

import com.effective.linkshorter.entity.LinkDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Validated
public class LinkShorterController {

    @PostMapping("/link")
    ResponseEntity<LinkDto> linkCutter(@NotNull @RequestBody LinkDto linkDto, HttpServletRequest request) {

        return ResponseEntity.ok().build();
    }
}
