package ru.skillbox.socnetwork.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.skillbox.socnetwork.model.rsdto.GeneralResponse;
import ru.skillbox.socnetwork.model.rsdto.TempResponseDto;
import ru.skillbox.socnetwork.model.rsdto.postdto.TagDTO;
import ru.skillbox.socnetwork.service.TagService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tags/")
public class TagController {

    private final TagService tagService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse<List<TagDTO>>> getTags(
            @RequestParam(value = "tag", defaultValue = "all") String tag,
            @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
            @RequestParam(value = "perPage", defaultValue = "20", required = false) int perPage) {

        return ResponseEntity.ok(new GeneralResponse<>(tagService.getTags(tag)));
    }
}
