package com.immo.service;

import com.immo.model.TypeTransaction;
import com.immo.repository.TypeTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TypeTransactionService {
    private final TypeTransactionRepository repository;

    public TypeTransactionService(TypeTransactionRepository repository) { 
        this.repository = repository; 
    }

    public List<TypeTransaction> findAll() { 
        return repository.findAll(); 
    }

    public Optional<TypeTransaction> findById(Long id) { 
        return repository.findById(id); 
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public TypeTransaction save(TypeTransaction typeTransaction) { 
        return repository.save(typeTransaction); 
    }

    public void deleteById(Long id) { 
        repository.deleteById(id); 
    }
}
