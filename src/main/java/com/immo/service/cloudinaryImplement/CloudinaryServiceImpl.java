package com.immo.service.cloudinaryImplement;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.immo.utils.Utils;
import com.immo.dto.AlbumRequest;
import com.immo.dto.ProprietaireRequest;
import com.immo.error.FileUploadException;
import com.immo.model.Album;
import com.immo.model.Maison;
import com.immo.model.Proprietaire;
import com.immo.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.base-folder:immobilier}")
    private String baseFolder;

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
    public String getPhotoUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }

    @Override
    public String getPhotoUrl(String publicId, int width, int height) {
        return cloudinary.url()
            .transformation(new Transformation<>()
                    .width(width)
                    .height(height)
                    .crop("fill"))
            .generate(publicId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadPhoto(MultipartFile file, Album album, String customFilename) {
        try {
            String folderPath = String.format("%s/%s/%s", baseFolder, album.getMaison().getProprietaire().getTelephone(), album.getNomAlbum());
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", folderPath);
            uploadParams.put("resource_type", "auto");
            uploadParams.put("overwrite", true);

            String publicId = generatePhotoPublicId(null);
            uploadParams.put("public_id", publicId);

            uploadParams.put("use_filename", false);
            uploadParams.put("unique_filename", false);

            return cloudinary.uploader().upload(file.getBytes(), uploadParams);
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
    public List<Map<String, Object>> getResourcesInFolder(String folderPath) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                "type", "upload",
                "prefix", folderPath + "/",
                "max_results", 100
            );
            // ✅ On précise bien le type ici
            Map<String, Object> result = cloudinary.api().resources(options);
            // ✅ Et on caste le contenu en List<Map<String, Object>>
            return (List<Map<String, Object>>) result.get("resources");
        } catch (Exception exc) {
            throw new FileUploadException(String.format("Erreur lors de la récupération des ressources du dossier %s : %s", folderPath, exc.getMessage()));
        }
    }


    public Map<String, Object> createAlbum(Maison maison, AlbumRequest albumRequest) {
        String folderPath = String.format(
            "%s/%s/%s",
            baseFolder,
            maison.getProprietaire().getTelephone(),
            albumRequest.getNomAlbum()
        );
        Map<String, Object> result = createFolder(folderPath);
        return result;
    }

    public  Map<String, Object> createProprietaire(ProprietaireRequest proprietaire) { 
        String folderPath = String.format(
            "%s/%s", 
            baseFolder, 
            proprietaire.getTelephone()
        );
        Map<String, Object> result = createFolder(folderPath);
        return result;
    }

    @Override
    public Map<String, Object> deleteAlbum(Album album) { 
        String folderPath = String.format(
            "%s/%s/%s", baseFolder, 
            album.getMaison().getProprietaire().getTelephone(), 
            album.getNomAlbum()
        );
        Map<String, Object> result =  deleteFolder(folderPath);
        return result;
    }

    @Override
    public Map<String, Object> deleteProprietaire(Proprietaire proprietaire) { 
        String folderPath = String.format(
            "%s/%s", 
            baseFolder, 
            proprietaire.getTelephone()
        );
        Map<String, Object> result =  deleteFolder(folderPath);
        return result;
    }

    
    @Override
    @SuppressWarnings("unchecked")
    public  Map<String, Object> deleteFolder(String folderPath) {
        try {
            // 1️⃣ Supprimer toutes les ressources dans le dossier
            cloudinary.api().deleteResourcesByPrefix(folderPath, ObjectUtils.emptyMap());
            // 2️⃣ Supprimer aussi les ressources dans les sous-dossiers
            cloudinary.api().deleteResourcesByPrefix(folderPath + "/", ObjectUtils.emptyMap());
            // 3️⃣ Supprimer le dossier lui-même
            Map<String, Object> result = cloudinary.api().deleteFolder(folderPath, ObjectUtils.emptyMap());
            return result;
        } catch (Exception exc) {
            throw new FileUploadException(String.format("Erreur lors de la suppression du dossier %s dans cloudinary : %s", folderPath, exc.getMessage()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> createFolder(String foderPath) {
        try {
            Map<String, Object> result = cloudinary.api().createFolder(foderPath,  ObjectUtils.emptyMap());
            return result;
        } catch (Exception exc) {
            throw new FileUploadException(String.format("Erreur lors de la creation du dossier %s dans cloudinary : %s", foderPath, exc.getMessage()));
        }
    }



    @SuppressWarnings("unchecked")
    public String renameAlbum(String ancienPath, Maison maison, AlbumRequest newAlbum) {
        try {
            // creation de l'album
            String proprietaire = maison.getProprietaire().getTelephone();
            String newFolderPath = String.format("%s/%s/%s", baseFolder, proprietaire, newAlbum.getNomAlbum());
            Map<String, Object> resultFolder = createFolder(newFolderPath);
            // on deplace le contenue de l'ancien album dans le nouveau album
            List<Map<String, Object>> resources = getResourcesInFolder(ancienPath);
            for (Map<String, Object> resource : resources) {
                String publicId = String.valueOf(resource.get("public_id"));
                String fileName = publicId.substring(publicId.lastIndexOf("/") + 1);
                String newPublicId = resultFolder.get("path") + "/" + fileName;
                // 1. Renommer le public_id (déplacer le fichier)
                Map<String, Object> renameOptions = ObjectUtils.asMap(
                    "overwrite", false,
                    "invalidate", true,
                    "resource_type", "image"
                );
                cloudinary.uploader().rename(publicId, newPublicId, renameOptions);
                // 2. MAINTENANT METTRE À JOUR L'ASSET_FOLDER POUR QUE LES FICHIER SE DEPLACE
                updateAssetFolder(newPublicId, newFolderPath);
            }
            // suprimer ancien dossier qui est vide
            cloudinary.api().deleteFolder(ancienPath, ObjectUtils.emptyMap());
            return newFolderPath;
        } catch (Exception exc) {
            throw new FileUploadException(String.format("Erreur lors du renommage de l'Album %s en %s dans cloudinary : %s", ancienPath.split("\\")[2], newAlbum.getNomAlbum(), exc.getMessage()));
        }
    }

    /**
     * Met à jour l'asset_folder d'une ressource Cloudinary
    */
    @Override
    @SuppressWarnings("unchecked")
    public  Map<String, Object> updateAssetFolder(String publicId, String newAssetFolder) {
        try {
            Map<String, Object> updateParams = ObjectUtils.asMap(
                "asset_folder", newAssetFolder,
                "display_name", publicId.substring(publicId.lastIndexOf("/") + 1),
                "context", true
            );
            Map<String, Object> result = cloudinary.api().update(publicId, updateParams);
            return result;
        } catch (Exception e) {
            throw new FileUploadException(String.format("Erreur mise à jour asset folder pour %s : %s", publicId, e.getMessage()));
        }
    }
}
