package com.immo.service;

import com.immo.dto.AlbumRequest;
import com.immo.dto.ProprietaireRequest;
import com.immo.model.Album;
import com.immo.model.Maison;
import com.immo.model.Proprietaire;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CloudinaryService {
    
    // Gestion des photos
    public Map<String, Object> deletePhoto(String publicId);
    public String getPhotoUrl(String publicId);
    public String getPhotoUrl(String publicId, int width, int height);
    public Map<String, Object> uploadPhoto(MultipartFile file, Album album, String customFilename);
    public String generatePhotoPublicId(String customName);
    
    // Gestion des albums
    public Map<String, Object> createAlbum(Maison maison, AlbumRequest album);
    public Map<String, Object> deleteAlbum(Album album);
    public String renameAlbum(String ancienPath, Maison maison, AlbumRequest newAlbum);
    public List<Map<String, Object>> getResourcesInFolder(String folderPath);
    public  Map<String, Object> updateAssetFolder(String publicId, String newAssetFolder);
    public Map<String, Object> createFolder(String foderPath);
    public  Map<String, Object> deleteFolder(String folderPath);
    
    // Gestion des propriétaires
    public Map<String, Object> deleteProprietaire(Proprietaire proprietaire);
    public  Map<String, Object> createProprietaire(ProprietaireRequest proprietaire);
}