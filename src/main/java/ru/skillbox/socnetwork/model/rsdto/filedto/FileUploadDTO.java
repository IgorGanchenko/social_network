package ru.skillbox.socnetwork.model.rsdto.filedto;

import com.dropbox.core.v2.files.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.skillbox.socnetwork.model.entity.Person;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class FileUploadDTO {
  private String id;
  private Integer ownerID;
  private String fileName;
  private String relativeFilePath;
  private String rawFileURL;
  private String fileFormat;
  private Long bytes;
  private FileType fileType;
  private Long createdAt;

  public FileUploadDTO(Person person, FileMetadata fileMetadata){
    id = fileMetadata.getId().substring(3);
    ownerID = person.getId();
    this.fileName = fileMetadata.getName();
    this.relativeFilePath = fileMetadata.getPathDisplay();
    this.rawFileURL = fileMetadata.getPathDisplay();
    fileFormat = getFileFormat(fileMetadata.getName());
    this.bytes = fileMetadata.getSize();
    fileType = FileType.IMAGE;
    createdAt = fileMetadata.getServerModified().getTime();
  }

  private String getFileFormat(String name){
    Pattern pattern = Pattern.compile(".*\\.([A-z]*)$");
    Matcher matcher = pattern.matcher(name);
    return (matcher.find()) ?  matcher.group(1) : "";
  }
}
