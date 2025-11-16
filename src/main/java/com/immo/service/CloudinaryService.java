package com.immo.service;

import com.immo.model.Album;
import java.util.List;
import java.util.Map;

public interface CloudinaryService {
    
    // Gestion des photos
    public Map<String, Object> deletePhoto(String publicId);
    public Map<String, Object> uploadPhoto(byte [] file, Album album); // ok
    public Map<String, Object> uploadPhoto(byte [] file, String folderPath); // ok
    public String generatePhotoPublicId(String customName); // ok
    
    // Gestion des albums
    public Map<String, Object> deleteAlbum(String folderPath);
    public Map<String, Object> createFolder(String foderPath); // ok
    public  Map<String, Object> deleteFolder(String folderPath);
    public List<Map<String, Object>> getResourcesInFolder(String folderPath);

    // Recuperation du base path
    public  String getBasePath();
}