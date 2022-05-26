package ru.skillbox.socnetwork.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.socnetwork.logging.InfoLogs;
import ru.skillbox.socnetwork.exception.InvalidRequestException;
import ru.skillbox.socnetwork.model.entity.Person;
import ru.skillbox.socnetwork.model.rsdto.postdto.NewPostDto;
import ru.skillbox.socnetwork.model.rsdto.DialogsResponse;
import ru.skillbox.socnetwork.model.rsdto.GeneralResponse;
import ru.skillbox.socnetwork.model.rsdto.PersonDto;
import ru.skillbox.socnetwork.model.rsdto.UpdatePersonDto;
import ru.skillbox.socnetwork.model.rsdto.postdto.PostDto;
import ru.skillbox.socnetwork.security.SecurityUser;
import ru.skillbox.socnetwork.service.PersonService;
import ru.skillbox.socnetwork.service.PostService;

import java.util.List;


@Log
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users/")
@InfoLogs
public class ProfileController {

    private final PersonService personService;
    private final PostService postService;

    @GetMapping(path = "me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse<PersonDto>> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
        String email = securityUser.getUsername();
        PersonDto personDto = new PersonDto(personService.getByEmail(email));
        return ResponseEntity.ok(new GeneralResponse<>(
                "string",
                System.currentTimeMillis(),
                personDto));
    }

    @PutMapping(path = "me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse<Person>> updateProfile(
            @RequestBody UpdatePersonDto updatePersonDto) {

        return ResponseEntity.ok(new GeneralResponse<>(
                "string",
                System.currentTimeMillis(),
                personService.updatePerson(updatePersonDto)
        ));
    }

    @DeleteMapping(path = "me")
    public ResponseEntity<GeneralResponse<DialogsResponse>> deleteProfile()
            throws InvalidRequestException {

        return ResponseEntity.ok(new GeneralResponse<>(
                "string",
                System.currentTimeMillis(),
                new DialogsResponse(personService.setBlockPerson())
        ));
    }

    @PutMapping(path = "me/return")
    public ResponseEntity<GeneralResponse<PersonDto>> returnProfile() {

        return ResponseEntity.ok(new GeneralResponse<>(
                "string",
                System.currentTimeMillis(),
                new PersonDto(personService.returnProfile())
        ));
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse<PersonDto>> getProfileById(@PathVariable int id) {
        PersonDto personDto = new PersonDto(personService.getById(id));
        return ResponseEntity.ok(new GeneralResponse<>(
                "string",
                System.currentTimeMillis(),
                personDto));
    }

    @GetMapping(path = "{id}/wall", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getWallByProfileId
            (@PathVariable int id,
             @RequestParam(value = "offset", defaultValue = "0") int offset,
             @RequestParam(value = "itemPerPage", defaultValue = "20") int perPage) {
        GeneralResponse<List<PostDto>> response = new GeneralResponse<>(postService.getWall(id, offset, perPage),
                postService.getPostCount(), offset, perPage);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "{id}/wall", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse<PostDto>> addNewPost
            (@PathVariable int id,
             @RequestParam(value = "publish_date", defaultValue = "-1") long publishDate,
             @RequestBody NewPostDto newPostDto) throws InvalidRequestException {
        newPostDto.setAuthorId(id);

        GeneralResponse<PostDto> response = new GeneralResponse<>(postService.addPost(newPostDto, publishDate));
        return ResponseEntity.ok(response);
    }

    @PutMapping("block/{id}")
    public ResponseEntity<GeneralResponse<DialogsResponse>> blockUser(@PathVariable Integer id)
            throws InvalidRequestException {

        GeneralResponse<DialogsResponse> generalResponse =
                new GeneralResponse<>("string", System.currentTimeMillis(), personService.blockUser(id));

        return ResponseEntity.ok(generalResponse);
    }

    @DeleteMapping("block/{id}")
    public ResponseEntity<GeneralResponse<DialogsResponse>> unblockUser(@PathVariable Integer id)
            throws InvalidRequestException {

        GeneralResponse<DialogsResponse> generalResponse =
                new GeneralResponse<>("string", System.currentTimeMillis(), personService.unblockUser(id));

        return ResponseEntity.ok(generalResponse);
    }

    @GetMapping("search")
    public ResponseEntity<GeneralResponse<List<PersonDto>>> searchByPeople(
            @RequestParam(value = "first_name", defaultValue = "", required = false) String firstName,
            @RequestParam(value = "last_name", defaultValue = "", required = false) String lastName,
            @RequestParam(value = "age_from", defaultValue = "0", required = false) long ageFrom,
            @RequestParam(value = "age_to", defaultValue = "150", required = false) long ageTo,
            @RequestParam(value = "country_id", defaultValue = "1", required = false) int countryId,
            @RequestParam(value = "city_id", defaultValue = "1", required = false) int cityId,
            @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
            @RequestParam(value = "itemPerPage", defaultValue = "20", required = false) int perPage){

            GeneralResponse<List<PersonDto>> response = new GeneralResponse<>
                    (personService.getPersonsBySearchParameters(firstName, lastName, ageFrom, ageTo,
                            countryId, cityId, perPage));
            return ResponseEntity.ok(response);
        }
}
