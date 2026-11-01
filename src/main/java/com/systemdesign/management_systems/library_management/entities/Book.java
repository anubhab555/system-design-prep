package com.systemdesign.management_systems.library_management.entities;

public class Book extends LibraryItem {
    private final String author;

    public Book(String id, String title, String author){
        super(id, title);
        this.author = author;
    }

    @Override 
    public String getAuthorOrPublisher(){ return author; }
}
