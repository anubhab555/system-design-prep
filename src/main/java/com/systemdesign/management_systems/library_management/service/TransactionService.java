package com.systemdesign.management_systems.library_management.service;

import java.util.HashMap;
import java.util.Map;

import com.systemdesign.management_systems.library_management.entities.BookCopy;
import com.systemdesign.management_systems.library_management.entities.Loan;
import com.systemdesign.management_systems.library_management.entities.Member;

public class TransactionService {
    private static final TransactionService INSTANCE = new TransactionService();
    private final Map<String, Loan> activeLoans = new HashMap<>();

    private TransactionService(){}

    public static TransactionService getInstance(){
        return INSTANCE;
    }
        
    public void createLoan(BookCopy copy, Member member){
        if(activeLoans.containsKey(copy.getId())){
            throw new IllegalStateException("This copy is already on loan.");
        }
        Loan loan = new Loan(copy, member);
        activeLoans.put(copy.getId(), loan);
        member.addLoan(loan);
    }

    public void endLoan(BookCopy copy) {
        Loan loan = activeLoans.remove(copy.getId());
        if (loan != null) {
            loan.getMember().removeLoan(loan);
        }
    }
}
