package com.systemdesign.management_systems.library_management.states;

import com.systemdesign.management_systems.library_management.entities.BookCopy;
import com.systemdesign.management_systems.library_management.entities.Member;
import com.systemdesign.management_systems.library_management.service.TransactionService;


public class CheckedOutState implements ItemState {
    
    @Override
    public void checkout(BookCopy copy, Member member){
        System.out.println(copy.getId() + " is already checked out.");
    }

    @Override
    public void returnItem(BookCopy copy){
        TransactionService.getInstance().endLoan(copy);
        System.out.println(copy.getId() + " returned.");
        if(copy.getItem().hasObservers()){
            copy.setState(new OnHoldState());
            copy.getItem().notifyObservers(); // Notify members that item is back but on hold
        } else {
            copy.setState(new AvailableState());
        }
    }

    @Override 
    public void placeHold(BookCopy copy, Member member){
        copy.getItem().addObserver(member);
        System.out.println(member.getName()+ " placed a hold on '" + copy.getItem().getTitle() + "'");
    }
}
