package com.example.mosque;

public class PrayerTimes {
    private int    id;
    private String fajr;
    private String dhuhr;
    private String asr;
    private String magrib;
    private String isha;
    private String fajrIqama;
    private String dhuhrIqama;
    private String asrIqama;
    private String magribIqama;
    private String ishaIqama;
    private String updatedAt;

    public PrayerTimes(int id, String fajr, String dhuhr, String asr,
                       String magrib, String isha, String fajrIqama, String dhuhrIqama,
                       String asrIqama, String magribIqama, String ishaIqama, String updatedAt) {
        this.id = id;
        this.fajr = fajr;
        this.dhuhr = dhuhr;
        this.asr = asr;
        this.magrib = magrib;
        this.isha = isha;
        this.fajrIqama = fajrIqama;
        this.dhuhrIqama = dhuhrIqama;
        this.asrIqama = asrIqama;
        this.magribIqama = magribIqama;
        this.ishaIqama = ishaIqama;
        this.updatedAt = updatedAt;
    }

    // getters
    public int    getId()        { return id; }
    public String getFajr()      { return fajr; }
    public String getDhuhr()     { return dhuhr; }
    public String getAsr()       { return asr; }
    public String getMagrib()    { return magrib; }
    public String getIsha()      { return isha; }
    public String getFajrIqama() { return fajrIqama; }
    public String getDhuhrIqama() { return dhuhrIqama; }
    public String getAsrIqama() { return asrIqama; }
    public String getMagribIqama() { return magribIqama; }
    public String getIshaIqama() { return ishaIqama; }
    public String getUpdatedAt() { return updatedAt; }
}
