package com.systemdesign.management_systems.library_management.states;

import com.systemdesign.management_systems.library_management.entities.BookCopy;
import com.systemdesign.management_systems.library_management.entities.Member;
import com.systemdesign.management_systems.library_management.service.TransactionService;


public class OnHoldState implements ItemState{
    
    @Override
    public void checkout(BookCopy copy, Member member){
        if(copy.getItem().isObserver(member)){
            TransactionService.getInstance().createLoan(copy, member);
            copy.getItem().removeObserver(member);
            copy.setState(new CheckedOutState());
            System.out.println("Hold fulfilled. " + copy.getId() + " checked out by " + member.getName());
        } else{
            System.out.println("This item is on hold for another member.");
        }
    }

    @Override
    public void returnItem(BookCopy c) {
        System.out.println("Invalid action. Item is on hold, not checked out.");
    }

    @Override
    public void placeHold(BookCopy c, Member m) {
        c.getItem().addObserver(m);
        System.out.println(m.getName() + " added to the wait list for '" + c.getItem().getTitle() + "'");
    }
}
