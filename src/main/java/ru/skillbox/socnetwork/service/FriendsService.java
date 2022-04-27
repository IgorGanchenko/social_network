package ru.skillbox.socnetwork.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.skillbox.socnetwork.exception.InvalidRequestException;
import ru.skillbox.socnetwork.logging.DebugLogs;
import ru.skillbox.socnetwork.model.entity.Friendship;
import ru.skillbox.socnetwork.model.entity.enums.TypeCode;
import ru.skillbox.socnetwork.model.rqdto.UserIdsDto;
import ru.skillbox.socnetwork.model.rsdto.FriendshipPersonDto;
import ru.skillbox.socnetwork.model.rsdto.MessageResponseDto;
import ru.skillbox.socnetwork.model.rsdto.PersonDto;
import ru.skillbox.socnetwork.repository.FriendshipRepository;
import ru.skillbox.socnetwork.repository.PersonRepository;
import ru.skillbox.socnetwork.security.SecurityUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@DebugLogs
public class FriendsService {

    private final PersonRepository personRepository;
    private final FriendshipRepository friendshipRepository;

    private SecurityUser getAuthorizedUser() {
        return (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<PersonDto> getListRecommendedFriends() {
        String email = getAuthorizedUser().getUsername();
        return personRepository.getListRecommendedFriends(email).stream()
                .map(PersonDto::new)
                .collect(Collectors.toList());
    }

    public List<PersonDto> getUserFriends() {
        String email = getAuthorizedUser().getUsername();
        return personRepository.getUserFriends(email).stream()
                .map(PersonDto::new)
                .collect(Collectors.toList());
    }

    public MessageResponseDto deleteFriendById(Integer friendId) throws InvalidRequestException {
        String email = getAuthorizedUser().getUsername();
        Integer authorizedUserId = personRepository.getByEmail(email).getId();
        int countFrom = friendshipRepository.removeFriendlyStatusByPersonIds(authorizedUserId, friendId);
        int countTo = friendshipRepository.removeFriendlyStatusByPersonIds(friendId, authorizedUserId);
        if (countFrom == 0 && countTo == 0) {
            throw new InvalidRequestException("Deletion is not possible. " +
                    "No friendly relationship found between the specified user.");
        }

        return new MessageResponseDto("ok");
    }

    public MessageResponseDto addFriendById(Integer focusPersonId) throws InvalidRequestException {
        String email = getAuthorizedUser().getUsername();
        Integer authorizedUserId = personRepository.getByEmail(email).getId();
        if (authorizedUserId.equals(focusPersonId)) {
            throw new InvalidRequestException("You can't send a request to yourself.");
        }

        Optional<Friendship> friendshipFromInitiator = friendshipRepository
                .getFriendlyStatusByPersonIds(authorizedUserId, focusPersonId);
        Optional<Friendship> friendshipFromFocusPerson = friendshipRepository
                .getFriendlyStatusByPersonIds(focusPersonId, authorizedUserId);

        if (friendshipFromInitiator.isPresent() || friendshipFromFocusPerson.isPresent()) {
            Friendship friendshipInitiator = friendshipFromInitiator.orElse(null);
            Friendship friendshipFocusPerson = friendshipFromFocusPerson.orElse(null);

            if ((friendshipInitiator != null && friendshipInitiator.getCode() == TypeCode.FRIEND) ||
                    (friendshipFocusPerson != null && friendshipFocusPerson.getCode() == TypeCode.FRIEND)) {
                throw new InvalidRequestException("It is not possible to apply as a friend, " +
                        "because these users are already friends.");

            } else if ((friendshipInitiator != null && friendshipInitiator.getCode() == TypeCode.BLOCKED) ||
                    (friendshipFocusPerson != null && friendshipFocusPerson.getCode() == TypeCode.BLOCKED)) {
                throw new InvalidRequestException("The request is not possible because the specified user is blocked.");

            } else if (friendshipInitiator != null && friendshipInitiator.getCode() == TypeCode.REQUEST) {
                throw new InvalidRequestException("It is not possible to submit a request to add as a friend, " +
                        "as it has already been submitted earlier.");

            } else if (friendshipInitiator == null && friendshipFocusPerson.getCode() == TypeCode.REQUEST) {
                    friendshipRepository.updateFriendlyStatusByPersonIdsAndCode(focusPersonId, authorizedUserId,
                            TypeCode.FRIEND.toString());
            }
        } else
            friendshipRepository.createFriendRequestByPersonIds(authorizedUserId, focusPersonId);

        return new MessageResponseDto("ok");
    }

    public List<PersonDto> getListIncomingFriendRequests() {
        String email = getAuthorizedUser().getUsername();
        return personRepository.getListIncomingFriendRequests(email).stream()
                .map(PersonDto::new)
                .collect(Collectors.toList());
    }

    public List<FriendshipPersonDto> getInformationAboutFriendships(UserIdsDto userIdsDto) {
        String email = getAuthorizedUser().getUsername();
        List<Integer> userIds = userIdsDto.getUserIds();
        return friendshipRepository.getInformationAboutFriendships(email, userIds);
    }
}
