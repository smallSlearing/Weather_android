package com.juhe.weather.bean;

/**
 * 视频的实体类
 */
public class Video {
    private Integer id; //id
    private String imgUrl;  //封面的路径
    private int videoUrl;  //视频的路径
    private Integer star; //点赞量

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(int videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }


}
