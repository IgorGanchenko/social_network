package ru.skillbox.socnetwork.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.socnetwork.model.rsdto.GeneralResponse;
import ru.skillbox.socnetwork.model.rsdto.postdto.PostDto;
import ru.skillbox.socnetwork.security.SecurityUser;
import ru.skillbox.socnetwork.service.PostService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feeds")
public class FeedsController {

    private final PostService postService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getFeeds(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                                     @RequestParam(value = "perPage", defaultValue = "20") int perPage) {

        GeneralResponse<List<PostDto>> response = new GeneralResponse<>(postService.getAll(offset, perPage, getSecurityUser().getId()));
        return ResponseEntity.ok(response);
    }

    private SecurityUser getSecurityUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) auth.getPrincipal();
    }
}
