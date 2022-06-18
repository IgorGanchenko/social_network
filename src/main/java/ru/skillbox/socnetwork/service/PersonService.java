package ru.skillbox.socnetwork.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skillbox.socnetwork.exception.ExceptionText;
import ru.skillbox.socnetwork.exception.InvalidRequestException;
import ru.skillbox.socnetwork.logging.DebugLogs;
import ru.skillbox.socnetwork.model.entity.DeletedUser;
import ru.skillbox.socnetwork.model.entity.Friendship;
import ru.skillbox.socnetwork.model.entity.Person;
import ru.skillbox.socnetwork.model.entity.TempToken;
import ru.skillbox.socnetwork.model.entity.enums.TypeCode;
import ru.skillbox.socnetwork.model.rqdto.LoginDto;
import ru.skillbox.socnetwork.model.rqdto.RegisterDto;
import ru.skillbox.socnetwork.model.rsdto.PersonDto;
import ru.skillbox.socnetwork.model.rsdto.UpdatePersonDto;
import ru.skillbox.socnetwork.repository.FriendshipRepository;
import ru.skillbox.socnetwork.repository.NotificationSettingsRepository;
import ru.skillbox.socnetwork.repository.PersonRepository;
import ru.skillbox.socnetwork.security.JwtTokenProvider;
import ru.skillbox.socnetwork.service.storage.StorageService;
import ru.skillbox.socnetwork.service.сaptcha.CaptchaService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@DebugLogs
public class PersonService implements ApplicationListener<AuthenticationSuccessEvent> {

    @Getter
    @Value("${skillbox.app.server}")
    private String host;

    @Getter
    @Value("${secretCaptcha}")
    private String secret;

    private final FriendshipRepository friendshipRepository;
    private final PersonRepository personRepository;

    private final StorageService storageService;
    private final TempTokenService tempTokenService;
    private final MailService mailService;
    private final DeletedUserService deletedUserService;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final CaptchaService captchaService;

    private final JwtTokenProvider tokenProvider;

    private final SecurityPerson securityPerson = new SecurityPerson();
    private static final HashMap<Integer, Long> lastOnlineTimeMap = new HashMap<>();

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String personName = securityPerson.getEmail();
        this.personRepository.updateLastOnlineTimeByEmail(personName, System.currentTimeMillis());
    }

    public PersonDto getCurrentPerson() {
        return getPersonDtoById(securityPerson.getPersonId());
    }

    public List<Person> getAll() {
        return personRepository.getAll();
    }

    public Person getByEmail(String email) {
        Person person = personRepository.getByEmail(email);
        if (isPersonOnline(person.getId())) {
            person.setLastOnlineTime(System.currentTimeMillis());
        }
        return person;
    }

    public boolean isEmptyEmail(String email) {
        return personRepository.isEmptyEmail(email);
    }

    public void saveFromRegistration(Person person) {
        Person newPerson = personRepository.saveFromRegistration(person);
        notificationSettingsRepository.addNotificationSettingsForNewUser(newPerson.getId());
    }

    public Person getById(int id) {
        Person person = personRepository.getById(id);
        if (isPersonOnline(id)) {
            person.setLastOnlineTime(System.currentTimeMillis());
        }
        return person;
    }

    public PersonDto getPersonDtoById(int id) {
        return new PersonDto(this.getById(id));
    }

    public void registration(RegisterDto registerDto) throws InvalidRequestException {
        if (!captchaService.isCorrectCode(registerDto)
//                || registerDto.getCode().equals(getSecret())
        ) {
            throw new InvalidRequestException(ExceptionText.INCORRECT_CAPTCHA.getMessage());
        }
        if (!isEmptyEmail(registerDto.getEmail())) {
            throw new InvalidRequestException(ExceptionText.INCORRECT_EMAIL.getMessage());
        }
        if (!isCorrectEmail(registerDto.getEmail())) {
            throw new InvalidRequestException(ExceptionText.INCORRECT_EMAIL.getMessage());
        }
        if (!registerDto.passwordsEqual()) {
            throw new InvalidRequestException(ExceptionText.INCORRECT_PASSWORD.getMessage());
        }
        Person person = new Person();
        person.setEmail(registerDto.getEmail());
        person.setPassword(new BCryptPasswordEncoder().encode(registerDto.getSecondPassword()));
        person.setFirstName(registerDto.getFirstName());
        person.setLastName(registerDto.getLastName());
//        person.setPhoto(storageService.getDefaultProfileImage());
        captchaService.removeCaptcha(registerDto.getCodeId());
        saveFromRegistration(person);
    }

    public PersonDto getPersonAfterLogin(LoginDto loginDto) throws InvalidRequestException {
        Person person = personRepository.getPersonAfterLogin(loginDto);
        if (person == null || person.getIsBlocked()) {
            return null;
        } else {
            addOnlinePerson(person.getId());
            person.setLastOnlineTime(System.currentTimeMillis());
            return new PersonDto(person,
                tokenProvider.generateToken(loginDto.getEmail()));
        }
    }
    public void updatePerson(UpdatePersonDto changedPerson) throws ParseException {
        Person updatablePerson = getAuthenticatedPerson();

        if(changedPerson.getFirstName() != null &&
            !changedPerson.getFirstName().equals(updatablePerson.getFirstName())){
            updatablePerson.setFirstName(changedPerson.getFirstName());
        }
        if (changedPerson.getLastName() != null &&
                !changedPerson.getLastName().equals(updatablePerson.getLastName())) {
            updatablePerson.setLastName(changedPerson.getLastName());
        }
        String date;
        if(changedPerson.getBirthDate() != null){
            date = changedPerson.getBirthDate().substring(0, 10);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            long dateTime = format.parse(date).getTime();
            if (dateTime != updatablePerson.getBirthDate()) {
                updatablePerson.setBirthDate(dateTime);
            }
        }
        if (!changedPerson.getPhone().isEmpty()) {
            String phone = (changedPerson.getPhone().charAt(0) == '7') ? changedPerson.getPhone() :
                    "7" + changedPerson.getPhone();
            if (!phone.equals(updatablePerson.getPhone())) {
                updatablePerson.setPhone(phone);
            }
        }
        if (changedPerson.getAbout() != null &&
                !changedPerson.getAbout().equals(updatablePerson.getAbout())) {
            updatablePerson.setAbout(changedPerson.getAbout());
        }
        if (changedPerson.getCity() != null &&
                !changedPerson.getCity().equals(updatablePerson.getCity())) {
            updatablePerson.setCity(changedPerson.getCity());
        }
        if (changedPerson.getCountry() != null &&
                !changedPerson.getCountry().equals(updatablePerson.getCountry())) {
            updatablePerson.setCountry(changedPerson.getCountry());
        }
        personRepository.updatePerson(updatablePerson);
    }

    public void updateEmail(Map<String, String> body) throws InvalidRequestException{
        String email = tempTokenService.getToken(body.get("token")).getEmail();
        Person person = getByEmail(email);
        if(person == null){
            throw new InvalidRequestException("User with email " + email + " not registered");
        }
        personRepository.updateEmail(person, body.get("email"));
        tempTokenService.deleteToken(body.get("token"));
        mailService.send(email, "Your email has been changed", "Your email changed successfully!");
        mailService.send(person.getEmail(), "Your email has been changed", "Your email changed successfully!");
    }

    public List<PersonDto> getPersonsBySearchParameters(String firstName, String lastName,
        long ageFrom, long ageTo,
        int countryId, int cityId,
        int perPage) {
        List<Person> persons = personRepository.getPersonsFromSearch(firstName, lastName, ageFrom, ageTo,
                countryId, cityId, perPage);

        List<PersonDto> personsDto = new ArrayList<>();
        for (Person person : persons) {
            if (isPersonOnline(person.getId())) {
                person.setLastOnlineTime(System.currentTimeMillis());
            }
            personsDto.add(new PersonDto(person));
        }
        return personsDto;
    }

    public void recoverEmail(String email) throws InvalidRequestException {
        Person person = getByEmail(email);
        if(person == null){
            throw new InvalidRequestException("User with email " + email + " not registered");
        }
        TempToken token = new TempToken(person.getEmail(), generateToken());
        tempTokenService.addToken(token);
        String link = getHost() + "/shift-email?token=" + token.getToken();
        mailService.send(email, "Your SocNetwork Email change link", link);
    }

    public void recoverPassword(String email) throws InvalidRequestException {
        Person person = getByEmail(email);
        if(person == null){
            throw new InvalidRequestException("User with email " + email + " not registered");
        }
        TempToken token = new TempToken(person.getEmail(), generateToken());
        tempTokenService.addToken(token);
        String link = getHost() + "/change-password?token=" + token.getToken();
        mailService.send(person.getEmail(), "SocNetwork Password recovery", link);
    }

    public void setPassword(Map<String, String> body) throws InvalidRequestException{
        if(body.get("token") == null){
            throw new InvalidRequestException("wrong recovery link");
        }
        TempToken token = tempTokenService.getToken(body.get("token"));
        if(token == null){
            throw new InvalidRequestException("invalid recovery token");
        }
        Person person = getByEmail(token.getEmail());
        person.setPassword(new BCryptPasswordEncoder().encode(body.get("password")));
        personRepository.updatePassword(person);
        tempTokenService.deleteToken(body.get("token"));

        mailService.send(person.getEmail(), "Your password has been changed", "Your password changed successfully! You can now log in with new password!");
    }

    private String generateToken(){
        RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange(65, 90).build();

        return generator.generate(10);
    }

    public void blockUser(Integer focusPersonId) throws InvalidRequestException {
        String email = securityPerson.getEmail();
        Integer authorizedUserId = personRepository.getByEmail(email).getId();
        if (focusPersonId.equals(authorizedUserId)) {
            throw new InvalidRequestException("You can't block yourself.");
        }

        Optional<Friendship> friendshipFromInitiator = friendshipRepository
                .getFriendlyStatusByPersonIds(authorizedUserId, focusPersonId);
        Optional<Friendship> friendshipFromFocusPerson = friendshipRepository
                .getFriendlyStatusByPersonIds(focusPersonId, authorizedUserId);

        if (friendshipFromInitiator.isPresent() || friendshipFromFocusPerson.isPresent()) {
            Friendship friendshipInitiator = friendshipFromInitiator.orElse(null);
            Friendship friendshipFocusPerson = friendshipFromFocusPerson.orElse(null);

            if ((friendshipInitiator != null && friendshipInitiator.getCode() == TypeCode.BLOCKED) ||
                    (friendshipFocusPerson != null && friendshipFocusPerson.getCode() == TypeCode.BLOCKED)) {

                throw new InvalidRequestException("Blocking is not possible, because user in relation to the current " +
                        "user is already blocked.");

            } else if (friendshipInitiator != null && friendshipInitiator.getCode() != TypeCode.BLOCKED) {
                friendshipRepository.updateFriendlyStatusByPersonIdsAndCode(authorizedUserId, focusPersonId,
                        TypeCode.BLOCKED.toString());
            } else if (friendshipFocusPerson != null && friendshipFocusPerson.getCode() != TypeCode.BLOCKED){
                friendshipRepository.fullUpdateFriendlyStatusByPersonIdsAndCode(focusPersonId, authorizedUserId,
                        TypeCode.BLOCKED.toString());
            }
        } else {
            friendshipRepository.createFriendlyStatusByPersonIdsAndCode(authorizedUserId, focusPersonId,
                    TypeCode.BLOCKED.toString());
        }
    }

    public void unblockUser(Integer focusPersonId) throws InvalidRequestException {
        String email = securityPerson.getEmail();
        Integer authorizedUserId = personRepository.getByEmail(email).getId();
        if (authorizedUserId.equals(focusPersonId)) {
            throw new InvalidRequestException("Unlocking in relation to yourself is not possible.");
        }

        Optional<Friendship> friendshipFromInitiator = friendshipRepository
                .getFriendlyStatusByPersonIds(authorizedUserId, focusPersonId);
        Optional<Friendship> friendshipFromFocusPerson = friendshipRepository
                .getFriendlyStatusByPersonIds(focusPersonId, authorizedUserId);

        if (friendshipFromInitiator.isPresent() || friendshipFromFocusPerson.isPresent()) {
            Friendship friendshipInitiator = friendshipFromInitiator.orElse(null);
            Friendship friendshipFocusPerson = friendshipFromFocusPerson.orElse(null);

            if (friendshipInitiator != null && friendshipInitiator.getCode() == TypeCode.BLOCKED) {
                friendshipRepository.removeFriendlyStatusByPersonIdsAndCode(authorizedUserId, focusPersonId,
                        TypeCode.BLOCKED.toString());

            } else if (friendshipFocusPerson != null && friendshipFocusPerson.getCode() == TypeCode.BLOCKED) {
                throw new InvalidRequestException("Unlocking is not possible. " +
                        "Unlocking can only be done by a focus person.");
            }
        } else {
            throw new InvalidRequestException("Blocking in relation to the focus person was not detected.");
        }
    }

    public void setBlockPerson() throws InvalidRequestException{
        Person person = getAuthenticatedPerson();

        if(person == null){
            throw new InvalidRequestException("User not registered");
        }
        deletedUserService.add(person);
        person.setIsDeleted(true);
        person.setFirstName("Deleted");
        person.setLastName("");
        personRepository.updatePerson(person);
        person.setPhoto(storageService.getDeletedProfileImage());
        personRepository.updatePhoto(person);

        mailService.send(person.getEmail(), "Your account will be deleted in 3 days!", "You requested to delete your account, it will be completely deleted in 3 days!");
    }

    public PersonDto restoreProfile(){
        Person person = getAuthenticatedPerson();

        DeletedUser deletedUser = deletedUserService.getDeletedUser(person.getId());

        person.setPhoto(deletedUser.getPhoto());
        person.setFirstName(deletedUser.getFirstName());
        person.setLastName(deletedUser.getLastName());
        personRepository.updatePerson(person);
        personRepository.updatePhoto(person);
        deletedUserService.delete(deletedUser.getId());

        mailService.send(person.getEmail(), "Your account restored!", "Your account was completely restored!");
        return new PersonDto(person);
    }

    public boolean isPersonOnline(Integer id) {
        if (lastOnlineTimeMap.isEmpty()) {
            return false;
        }
        return lastOnlineTimeMap.containsKey(id);
    }

    public void addOnlinePerson(Integer personId) {
        lastOnlineTimeMap.put(personId, System.currentTimeMillis());
        if (isTimeToCheckOnlineMap()) {
            removeOnlinePerson();
        }
    }

    public void removeOnlinePerson() {
        HashMap<Integer, Long> onlineMap = new HashMap<>();
        List<Integer> offlineMap = new ArrayList<>();
        lastOnlineTimeMap.forEach((id, time) -> {
            if (time < System.currentTimeMillis() - Constants.FIFTY_SECONDS_IN_MILLIS) {
                offlineMap.add(id);
            } else {
                onlineMap.put(id, time);
            }
        });
        lastOnlineTimeMap.clear();
        lastOnlineTimeMap.putAll(onlineMap);
        if (!offlineMap.isEmpty()) {
        personRepository.updateLastOnlineTimeFromMap(offlineMap);
        }
    }

    private boolean isTimeToCheckOnlineMap() {
        long l = Math.floorDiv(System.currentTimeMillis(), 1000);
        int i = Math.round((float) Math.sin(l) * 10);

        return i == 1;
    }

    private boolean isCorrectEmail(String email) {
        Pattern p = Pattern.compile(
                "^(?=(.{1,64}@.{1,64}))[.\\dA-Za-z_-]+@[.\\dA-Za-z_-]+([.][A-Za-z]{2,})$");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private Person getAuthenticatedPerson(){
        return getById(securityPerson.getPersonId());
    }
}
