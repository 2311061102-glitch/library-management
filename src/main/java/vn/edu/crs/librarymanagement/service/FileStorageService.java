package vn.edu.crs.librarymanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";

    /**
     * Lưu file và trả về đường dẫn tương đối để lưu vào DB (vd: "uploads/uuid_filename.png")
     */
    public String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueName = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
        Path target = uploadPath.resolve(uniqueName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return UPLOAD_DIR + "/" + uniqueName;
    }

    private String sanitize(String original) {
        if (original == null) return "file";
        return original.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}