package iss.ibf.pfm_expenses_server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iss.ibf.pfm_expenses_server.repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transRepo;

    public List<String> getCurrencies() {

        return this.transRepo.getCurrencies();
    }
    
}
