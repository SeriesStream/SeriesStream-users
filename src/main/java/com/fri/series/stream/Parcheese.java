package com.fri.series.stream;

public class Parcheese {

    private int id;
    private int episodeId;
    private int userId;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getEpisodeId() {
        return episodeId;
    }
    public void setEpisodeId(int episode) {
        this.episodeId = episode;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int id) {
        this.userId = id;
    }

    public Parcheese(int id, int ep, int user){
        setId(id);
        setEpisodeId(ep);
        setUserId(user);
    }
}
