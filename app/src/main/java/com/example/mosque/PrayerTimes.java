package com.example.mosque;

public class PrayerTimes {
    private int    id;
    private String fajr;
    private String dhuhr;
    private String asr;
    private String magrib;
    private String isha;
    private String updatedAt;

    public PrayerTimes(int id, String fajr, String dhuhr, String asr,
                       String magrib, String isha, String updatedAt) {
        this.id        = id;
        this.fajr      = fajr;
        this.dhuhr     = dhuhr;
        this.asr       = asr;
        this.magrib    = magrib;
        this.isha      = isha;
        this.updatedAt = updatedAt;
    }

    // getters
    public int    getId()        { return id; }
    public String getFajr()      { return fajr; }
    public String getDhuhr()     { return dhuhr; }
    public String getAsr()       { return asr; }
    public String getMagrib()    { return magrib; }
    public String getIsha()      { return isha; }
    public String getUpdatedAt() { return updatedAt; }
}
