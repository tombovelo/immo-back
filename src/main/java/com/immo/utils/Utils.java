package com.immo.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.immo.dto.AlbumResponse;
import com.immo.dto.AuthResponse;
import com.immo.dto.MaisonResponse;
import com.immo.dto.PhotoResponse;
import com.immo.dto.ProprietaireResponse;
import com.immo.dto.UserProfile;
import com.immo.dto.UtilisateurResponse;
import com.immo.error.NotFoundException;
import com.immo.model.Album;
import com.immo.model.Maison;
import com.immo.model.Photo;
import com.immo.model.Proprietaire;
import com.immo.model.Utilisateur;

/// GESTION GEOMETRIQUE COTE JAVA /////
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;

public class Utils {

    public static Map<String, String> parseFilename(String filename) {
        Map<String, String> result = new HashMap<>();
        // Séparer le nom et l'extension (suppose toujours une extension)
        int lastDotIndex = filename.lastIndexOf('.');
        String nameWithoutExt = filename.substring(0, lastDotIndex);
        String extension = filename.substring(lastDotIndex + 1);
        // Nettoyer le nom (sans l'extension)
        String cleanName = nameWithoutExt
            .replaceAll("\\s+", "_")                   
            .replaceAll("[^a-zA-Z0-9_\\-]", "_")
            .toLowerCase();                     
        // Construire le nom complet
        String fullname = cleanName + "." + extension;
        // Retourner les résultats
        result.put("nameWithoutExt", cleanName);
        result.put("fullName", fullname);
        return result;
    }

    /**
     * Génère un Public ID simple avec timestamp
     */
    public static String generateTimestampedId(String prefix) {
        String cleanPrefix = sanitize(prefix != null ? prefix : "img");
        return String.format("%s_%d", cleanPrefix, System.currentTimeMillis());
    }
    
    /**
     * Génère un Public ID avec UUID
     */
    public static String generateUniqueId(String prefix) {
        String cleanPrefix = sanitize(prefix != null ? prefix : "img");
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        return String.format("%s_%s", cleanPrefix, uuid);
    }
    
    /**
     * Nettoyage du texte pour Cloudinary
     */
    public static String sanitize(String text) {
        return text.toLowerCase()
            .replaceAll("\\s+", "_")
            .replaceAll("[^a-z0-9_]", "")
            .replaceAll("_{2,}", "_")
            .replaceAll("^_|_$", "");
    }

    public static ProprietaireResponse convertToResponse(Proprietaire proprietaire) {
        ProprietaireResponse dto = new ProprietaireResponse();
        dto.setId(proprietaire.getId());
        dto.setNom(proprietaire.getNom());
        dto.setPrenom(proprietaire.getPrenom());
        dto.setTelephone(proprietaire.getTelephone());
        dto.setAdresse(proprietaire.getAdresse());
        dto.setDateCreation(proprietaire.getDateCreation());

        if (proprietaire.getMaisons() != null) {
            dto.setMaisons(Utils.convertMaisonToResponse(proprietaire.getMaisons()));
        }
        // Conversion de l'utilisateur sans password
        if (proprietaire.getUtilisateur() != null) {
            dto.setUtilisateur(Utils.convertToResponse(proprietaire.getUtilisateur()));
        }
        return dto;
    }
    
    public static List<MaisonResponse> convertMaisonToResponse(List<Maison> maisons) {

        List<MaisonResponse> responses = new ArrayList<>();

        for(Maison maison : maisons) {
            MaisonResponse maisonResponse = new MaisonResponse();
            maisonResponse.setId(maison.getId());
            maisonResponse.setAdresse(maison.getAdresse());
            maisonResponse.setVille(maison.getVille());
            maisonResponse.setCodePostal(maison.getCodePostal());
            maisonResponse.setNombrePieces(maison.getNombrePieces());
            maisonResponse.setPrix(maison.getPrix());
            maisonResponse.setDescription(maison.getDescription());
            maisonResponse.setVisible(maison.getVisible());

            if (maison.getCoordinate() != null) {
                maisonResponse.setLatitude(maison.getCoordinate().getY());
                maisonResponse.setLongitude(maison.getCoordinate().getX());
            }
    
            maisonResponse.setDateCreation(maison.getDateCreation());
            maisonResponse.setTypeTransaction(maison.getTypeTransaction());
            // Conversion légère du propriétaire pour éviter la récursion
            if (maison.getProprietaire() != null) {
                ProprietaireResponse proprietaireLight = new ProprietaireResponse();
                proprietaireLight.setId(maison.getProprietaire().getId());
                proprietaireLight.setNom(maison.getProprietaire().getNom());
                proprietaireLight.setPrenom(maison.getProprietaire().getPrenom());
                proprietaireLight.setTelephone(maison.getProprietaire().getTelephone());
                proprietaireLight.setAdresse(maison.getProprietaire().getAdresse());
                proprietaireLight.setDateCreation(maison.getProprietaire().getDateCreation());
                proprietaireLight.setUtilisateur(Utils.convertToResponse(maison.getProprietaire().getUtilisateur()));
                // Ne pas inclure les maisons pour éviter la récursion
                maisonResponse.setProprietaire(proprietaireLight);
            }
            
            maisonResponse.setAlbums(Utils.convertAlumToResponse(maison.getAlbums()));
            responses.add(maisonResponse);
        }
        return responses;
    }
    
    public static UtilisateurResponse convertToResponse(Utilisateur utilisateur) {
        UtilisateurResponse userResponse = new UtilisateurResponse();
        userResponse.setId(utilisateur.getId());
        userResponse.setEmail(utilisateur.getEmail());
        userResponse.setRole(utilisateur.getRole());
        return userResponse;
    }
    
    public static MaisonResponse convertToResponse(Maison maison) {
        MaisonResponse maisonResponse = new MaisonResponse();
        maisonResponse.setId(maison.getId());
        maisonResponse.setAdresse(maison.getAdresse());
        maisonResponse.setVille(maison.getVille());
        maisonResponse.setCodePostal(maison.getCodePostal());
        maisonResponse.setNombrePieces(maison.getNombrePieces());
        maisonResponse.setPrix(maison.getPrix());
        maisonResponse.setDescription(maison.getDescription());
        maisonResponse.setVisible(maison.getVisible());

        if (maison.getCoordinate() != null) {
            maisonResponse.setLatitude(maison.getCoordinate().getY());
            maisonResponse.setLongitude(maison.getCoordinate().getX());
        }

        maisonResponse.setDateCreation(maison.getDateCreation());
        maisonResponse.setTypeTransaction(maison.getTypeTransaction());
        if (maison.getProprietaire() != null) {
            ProprietaireResponse proprietaireLight = new ProprietaireResponse();
            proprietaireLight.setId(maison.getProprietaire().getId());
            proprietaireLight.setNom(maison.getProprietaire().getNom());
            proprietaireLight.setPrenom(maison.getProprietaire().getPrenom());
            proprietaireLight.setTelephone(maison.getProprietaire().getTelephone());
            proprietaireLight.setAdresse(maison.getProprietaire().getAdresse());
            proprietaireLight.setDateCreation(maison.getProprietaire().getDateCreation());
            proprietaireLight.setUtilisateur(Utils.convertToResponse(maison.getProprietaire().getUtilisateur()));
            // Ne pas inclure les maisons pour éviter la récursion
            maisonResponse.setProprietaire(proprietaireLight);
        }
        if (maison.getAlbums() != null) {
            maisonResponse.setAlbums(Utils.convertAlumToResponse(maison.getAlbums()));
        }
        return maisonResponse;
    }
    
    public static List<AlbumResponse> convertAlumToResponse(List<Album> albums) {
        List<AlbumResponse> responses = new ArrayList<>();
        for(Album album : albums) {
            AlbumResponse albumResponse = new AlbumResponse();
            albumResponse.setId(album.getId());
            if (album.getMaison() != null) {
                MaisonResponse maisonLight = new MaisonResponse();
                maisonLight.setId(album.getMaison().getId());
                maisonLight.setAdresse(album.getMaison().getAdresse());
                maisonLight.setVille(album.getMaison().getVille());
                maisonLight.setCodePostal(album.getMaison().getCodePostal());
                maisonLight.setNombrePieces(album.getMaison().getNombrePieces());
                maisonLight.setPrix(album.getMaison().getPrix());
                maisonLight.setDescription(album.getMaison().getDescription());
                maisonLight.setVisible(album.getMaison().getVisible());

                if (album.getMaison().getCoordinate() != null) {
                    maisonLight.setLatitude(album.getMaison().getCoordinate().getY());
                    maisonLight.setLongitude(album.getMaison().getCoordinate().getX());
                }

                maisonLight.setDateCreation(album.getMaison().getDateCreation());
                maisonLight.setTypeTransaction(album.getMaison().getTypeTransaction());
                // Ne pas inclure les albums pour éviter la récursion
                albumResponse.setMaison(maisonLight);
            }
            
            albumResponse.setNomAlbum(album.getNomAlbum());
            albumResponse.setPath(album.getPath());
            albumResponse.setDescription(album.getDescription());
            albumResponse.setDateCreation(album.getDateCreation());
            albumResponse.setDateModification(album.getDateModification());

            if (album.getPhotos() != null) {
                albumResponse.setPhotos(Utils.convertPhotoToResponse(album.getPhotos()));
            }
    
            responses.add(albumResponse);
        }
        return responses;
    }
    
    public static AlbumResponse convertToResponse(Album album) {
        AlbumResponse albumResponse = new AlbumResponse();
        albumResponse.setId(album.getId());
        if (album.getMaison() != null) {
            MaisonResponse maisonLight = new MaisonResponse();
            maisonLight.setId(album.getMaison().getId());
            maisonLight.setAdresse(album.getMaison().getAdresse());
            maisonLight.setVille(album.getMaison().getVille());
            maisonLight.setCodePostal(album.getMaison().getCodePostal());
            maisonLight.setNombrePieces(album.getMaison().getNombrePieces());
            maisonLight.setPrix(album.getMaison().getPrix());
            maisonLight.setDescription(album.getMaison().getDescription());
            maisonLight.setVisible(album.getMaison().getVisible());

            if (album.getMaison().getCoordinate() != null) {
                maisonLight.setLatitude(album.getMaison().getCoordinate().getY());
                maisonLight.setLongitude(album.getMaison().getCoordinate().getX());
            }
        
            maisonLight.setDateCreation(album.getMaison().getDateCreation());
            maisonLight.setTypeTransaction(album.getMaison().getTypeTransaction());
            if (album.getMaison().getProprietaire() != null) {
                maisonLight.setProprietaire(Utils.convertToResponse(album.getMaison().getProprietaire()));
            }
            // Ne pas inclure les albums pour éviter la récursion
            albumResponse.setMaison(maisonLight);
        }
        albumResponse.setNomAlbum(album.getNomAlbum());
        albumResponse.setPath(album.getPath());
        albumResponse.setDescription(album.getDescription());
        albumResponse.setDateCreation(album.getDateCreation());
        albumResponse.setDateModification(album.getDateModification());

        if (album.getPhotos() != null) {
            albumResponse.setPhotos(Utils.convertPhotoToResponse(album.getPhotos()));
        }

        return albumResponse;
    }
    
    public static List<PhotoResponse> convertPhotoToResponse(List<Photo> photos) {
        List<PhotoResponse> responses = new ArrayList<>();
        for(Photo photo : photos) {
            PhotoResponse photoResponse = new PhotoResponse();
            photoResponse.setId(photo.getId());
            if (photo.getAlbum() != null) {
                AlbumResponse albumLight = new AlbumResponse();
                albumLight.setId(photo.getAlbum().getId());
                albumLight.setNomAlbum(photo.getAlbum().getNomAlbum());
                albumLight.setPath(photo.getAlbum().getPath());
                albumLight.setDescription(photo.getAlbum().getDescription());
                albumLight.setDateCreation(photo.getAlbum().getDateCreation());
                albumLight.setDateModification(photo.getAlbum().getDateModification());
                // Ne pas inclure les photos pour éviter la récursion
                photoResponse.setAlbum(albumLight);
            }
            photoResponse.setNomFichier(photo.getNomFichier());
            photoResponse.setCloudinaryPublicId(photo.getCloudinaryPublicId());
            photoResponse.setCloudinaryUrl(photo.getCloudinaryUrl());
            photoResponse.setDescription(photo.getDescription());
            photoResponse.setOrdre(photo.getOrdre());
            photoResponse.setDateCreation(photo.getDateCreation());
            photoResponse.setDateModification(photo.getDateModification());
            responses.add(photoResponse);
        }
        return responses;
    }
    
    public static PhotoResponse convertToResponse(Photo photo) {
        PhotoResponse photoResponse = new PhotoResponse();
        photoResponse.setId(photo.getId());
        if (photo.getAlbum() != null) {
            AlbumResponse albumLight = new AlbumResponse();
            albumLight.setId(photo.getAlbum().getId());
            albumLight.setNomAlbum(photo.getAlbum().getNomAlbum());
            albumLight.setPath(photo.getAlbum().getPath());
            albumLight.setDescription(photo.getAlbum().getDescription());
            albumLight.setDateCreation(photo.getAlbum().getDateCreation());
            albumLight.setDateModification(photo.getAlbum().getDateModification());
            albumLight.setMaison(Utils.convertToResponse(photo.getAlbum().getMaison()));
            // Ne pas inclure les photos pour éviter la récursion
            photoResponse.setAlbum(albumLight);
        }
        photoResponse.setNomFichier(photo.getNomFichier());
        photoResponse.setCloudinaryPublicId(photo.getCloudinaryPublicId());
        photoResponse.setCloudinaryUrl(photo.getCloudinaryUrl());
        photoResponse.setDescription(photo.getDescription());
        photoResponse.setOrdre(photo.getOrdre());
        photoResponse.setDateCreation(photo.getDateCreation());
        photoResponse.setDateModification(photo.getDateModification());
        return photoResponse;
    }

    public static Point convertToPoint(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            throw new NotFoundException("Longitude et latitude ne doivent pas être nulles");
        }
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    public static UserProfile mapToUserProfile(Proprietaire proprietaire) {
        UserProfile profile = new UserProfile();
        if (proprietaire.getUtilisateur() != null) {
            profile.setId(proprietaire.getUtilisateur().getId());
            profile.setEmail(proprietaire.getUtilisateur().getEmail());
            profile.setRole(proprietaire.getUtilisateur().getRole());
        }
        profile.setProprietaireId(proprietaire.getId());
        profile.setNom(proprietaire.getNom());
        profile.setPrenom(proprietaire.getPrenom());
        profile.setTelephone(proprietaire.getTelephone());
        profile.setAdresse(proprietaire.getAdresse());
        return profile;
    }

    public static AuthResponse mapToAuthResponse(UserProfile userProfile, String type, String token) {
        AuthResponse authResponse = new AuthResponse();
        UserProfile newUserProfile = new UserProfile();
        newUserProfile.setId(userProfile.getId());
        newUserProfile.setEmail(userProfile.getEmail());
        newUserProfile.setRole(userProfile.getRole());
        authResponse.setUserProfile(newUserProfile);
        authResponse.setType(type);
        authResponse.setToken(token);
        return authResponse;
    }   
        
}
