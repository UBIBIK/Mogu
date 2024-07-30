package com.example.mogu.object;

public class Place {
    private String imageUrl;
    private String name;
    private String address;

    public Place(String imageUrl, String name, String address) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
