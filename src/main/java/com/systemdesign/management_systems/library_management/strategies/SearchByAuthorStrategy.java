package com.systemdesign.management_systems.library_management.strategies;

import java.util.List;
import java.util.stream.Collectors;

import com.systemdesign.management_systems.library_management.entities.LibraryItem;

public class SearchByAuthorStrategy implements SearchStrategy {
    @Override 
    public List<LibraryItem> search(String query, List<LibraryItem> items){
        return items.stream().filter(item -> item.getAuthorOrPublisher().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
    }
}
