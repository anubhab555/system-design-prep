package com.systemdesign.management_systems.library_management.strategies;

import java.util.List;

import com.systemdesign.management_systems.library_management.entities.LibraryItem;

public interface SearchStrategy {
    List<LibraryItem> search(String query, List<LibraryItem> items);
}
