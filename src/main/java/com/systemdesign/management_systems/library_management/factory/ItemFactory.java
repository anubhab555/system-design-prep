package com.systemdesign.management_systems.library_management.factory;

import com.systemdesign.management_systems.library_management.entities.LibraryItem;
import com.systemdesign.management_systems.library_management.entities.Magazine;
import com.systemdesign.management_systems.library_management.entities.Book;
import com.systemdesign.management_systems.library_management.enums.ItemType;

public class ItemFactory {
    public static LibraryItem createItem(ItemType type, String id, String title, String authorOrPublisher){
        switch (type){
            case BOOK: return new Book(id, title, authorOrPublisher);
            case MAGAZINE: return new Magazine(id, title, authorOrPublisher);
            default: throw new IllegalArgumentException("Unknown item type.");  
        }
    }
}
