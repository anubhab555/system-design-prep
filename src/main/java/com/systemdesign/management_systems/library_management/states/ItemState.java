package com.systemdesign.management_systems.library_management.states;

import com.systemdesign.management_systems.library_management.entities.BookCopy;
import com.systemdesign.management_systems.library_management.entities.Member;

public interface ItemState {
    public void checkout(BookCopy copy, Member member);
    public void returnItem(BookCopy copy);
    public void placeHold(BookCopy copy, Member member);
}
