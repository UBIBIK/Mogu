package com.example.mogu.object;

public class TourApi {
    private String firstimage;
    private String title;
    private String addr1;
    private String addr2;
    private String contentid;
    private double mapx;
    private double mapy;

    public TourApi(String firstimage, String title, String addr1, String addr2, String contentid, double mapx, double mapy) {
        this.firstimage = firstimage;
        this.title = title;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.contentid = contentid;
        this.mapx = mapx;
        this.mapy = mapy;
    }

    // Getter methods
    public String getFirstimage() {
        return firstimage;
    }

    public String getTitle() {
        return title;
    }

    public String getAddr1() {
        return addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public String getContentid() {
        return contentid;
    }

    public double getMapx() {return mapx;}

    public double getMapy() {return mapy;}
}
