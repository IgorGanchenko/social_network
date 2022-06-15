package ru.skillbox.socnetwork.service.storage;

import lombok.AllArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.skillbox.socnetwork.model.entity.Person;
import ru.skillbox.socnetwork.repository.PersonRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class StorageCache {

  private final PersonRepository personRepository;
  private static RMap<String, String> cache;

  @Scheduled(initialDelayString = "PT5M", fixedDelay=Long.MAX_VALUE)
  private void initCache(){
    RedissonClient redissonClient = connect();
    System.err.println(redissonClient);
    cache = redissonClient.getMap("image-cache");
    cache.clear();
    cache.put(StorageConstants.PHOTO_DEFAULT, StorageConstants.PHOTO_DEFAULT_LINK);
    cache.put(StorageConstants.PHOTO_DELETED, StorageConstants.PHOTO_DELETED_LINK);
    cache.putAll(getPhotos());
  }

  public String addLink(String fileName, String link){
    cache.put(fileName, link);
    return cache.get(fileName);
  }

  public String getLink(String fileName){
    return cache.get(fileName);
  }

  public Boolean isLinkExists(String fileName){
    return cache.containsKey(fileName);
  }

  public void deleteLink(String fileName){
    cache.remove(fileName);
  }

  /*
  TODO указать путь в переменной. Лучше ip сервера.
   */
  private static RedissonClient connect(){
    Config config = new Config();
    config.useSingleServer().setAddress("redis://127.0.0.1:6379");
    return Redisson.create(config);
  }

  private Map<String, String> getPhotos(){
    Map<String, String> photos = new HashMap<>();
    List<Person> personList = personRepository.getAll();
    for(Person person : personList){
      photos.put(getRelativePath(person.getPhoto()), person.getPhoto());
    }
    return photos;
  }

  private String getRelativePath(String path) {
    Pattern pattern = Pattern.compile(".*(/\\w*\\.[A-z]*)\\?raw=1");
    Matcher matcher = pattern.matcher(path);
    return (matcher.find()) ? matcher.group(1) : "";
  }
}
