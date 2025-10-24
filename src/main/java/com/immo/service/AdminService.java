package com.immo.service;

import com.immo.model.Admin;
import com.immo.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository repository;

    public AdminService(AdminRepository repository) { 
        this.repository = repository; 
    }

    public List<Admin> findAll() { 
        return repository.findAll(); 
    }

    public Optional<Admin> findById(Long id) { 
        return repository.findById(id); 
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public Admin save(Admin admin) { 
        return repository.save(admin); 
    }

    public void deleteById(Long id) { 
        repository.deleteById(id); 
    }
}

