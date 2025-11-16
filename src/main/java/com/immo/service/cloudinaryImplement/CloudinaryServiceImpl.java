package com.immo.service.cloudinaryImplement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.immo.utils.Utils;
import com.immo.error.FileUploadException;
import com.immo.model.Album;
import com.immo.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.base-folder:immobilier}")
    private String baseFolder;

    public String getBasePath() {
        return baseFolder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> deletePhoto(String publicId) {
        try {
            return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception exc) {
            throw new FileUploadException("Erreur lors supression dans cloudinary : " + exc.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadPhoto(byte[] file, Album album) {
        try {
            // String folderPath = String.format("%s/%s/%s", baseFolder,
            // album.getMaison().getProprietaire().getTelephone(), album.getNomAlbum());
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", album.getPath());
            uploadParams.put("resource_type", "auto");
            uploadParams.put("overwrite", true);

            String publicId = generatePhotoPublicId(null);
            uploadParams.put("public_id", publicId);

            uploadParams.put("use_filename", false);
            uploadParams.put("unique_filename", false);

            return cloudinary.uploader().upload(file, uploadParams);
        } catch (Exception exc) {
            throw new FileUploadException("Erreur lors de l'upload du fichier vers Cloudinary : " + exc.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadPhoto(byte[] file, String folderPath) {
        try {
            // String folderPath = String.format("%s/%s/%s", baseFolder,
            // album.getMaison().getProprietaire().getTelephone(), album.getNomAlbum());
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", folderPath);
            uploadParams.put("resource_type", "auto");
            uploadParams.put("overwrite", true);

            String publicId = generatePhotoPublicId(null);
            uploadParams.put("public_id", publicId);

            uploadParams.put("use_filename", false);
            uploadParams.put("unique_filename", false);

            return cloudinary.uploader().upload(file, uploadParams);
        } catch (Exception exc) {
            throw new FileUploadException("Erreur lors de l'upload du fichier vers Cloudinary : " + exc.getMessage());
        }
    }

    @Override
    public String generatePhotoPublicId(String customName) {
        if (customName != null && !customName.trim().isEmpty()) {
            return Utils.sanitize(customName);
        } else {
            return Utils.generateTimestampedId("photo");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteFolder(String folderPath) {
        try {
            // 1️⃣ Supprimer toutes les ressources dans le dossier
            cloudinary.api().deleteResourcesByPrefix(folderPath, ObjectUtils.emptyMap());
            // 2️⃣ Supprimer aussi les ressources dans les sous-dossiers
            cloudinary.api().deleteResourcesByPrefix(folderPath + "/", ObjectUtils.emptyMap());
            // 3️⃣ Supprimer le dossier lui-même
            Map<String, Object> result = cloudinary.api().deleteFolder(folderPath, ObjectUtils.emptyMap());
            return result;
        } catch (Exception exc) {
            throw new FileUploadException(String.format(
                    "Erreur lors de la suppression du dossier %s dans cloudinary : %s", folderPath, exc.getMessage()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getResourcesInFolder(String folderPath) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "type", "upload",
                    "prefix", folderPath + "/",
                    "max_results", 100);
            // ✅ On précise bien le type ici
            Map<String, Object> result = cloudinary.api().resources(options);
            // ✅ Et on caste le contenu en List<Map<String, Object>>
            return (List<Map<String, Object>>) result.get("resources");
        } catch (Exception exc) {
            throw new FileUploadException(String.format(
                    "Erreur lors de la récupération des ressources du dossier %s : %s", folderPath, exc.getMessage()));
        }
    }

    @Override
    public Map<String, Object> deleteAlbum(String folderPath) {
        Map<String, Object> result = deleteFolder(folderPath);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> createFolder(String foderPath) {
        try {
            Map<String, Object> result = cloudinary.api().createFolder(foderPath, ObjectUtils.emptyMap());
            return result;
        } catch (Exception exc) {
            throw new FileUploadException(String.format("Erreur lors de la creation du dossier %s dans cloudinary : %s",
                    foderPath, exc.getMessage()));
        }
    }
}
