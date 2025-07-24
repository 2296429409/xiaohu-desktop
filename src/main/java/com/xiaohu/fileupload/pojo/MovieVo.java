package com.xiaohu.fileupload.pojo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xiaxh
 * @date 2025/7/16
 */


@Data
public class MovieVo {
    private String id;
    private String title;
    private String img;
    private ImageSize imageSize;
    private Date date;
    private String videoLength;
    private Director director;
    private Producer producer;
    private Publisher publisher;
    private Series series;
    private List<Genre> genres;
    private List<Star> stars;
    private List<Sample> samples;
    private List<SimilarMovie> similarMovies;
    private String gid;
    private String uc;


    @Data
    static class ImageSize {
        private Integer width;
        private Integer height;
    }

    @Data
    static class Director {
        private String id;
        private String name;
    }

    @Data
    static class Producer {
        private String id;
        private String name;
    }

    @Data
    static class Publisher {
        private String id;
        private String name;
    }

    @Data
    static class Series {
        private String id;
        private String name;
    }

    @Data
    public static class Genre {
        private String id;
        private String name;
    }

    @Data
    public static class Star {
        private String id;
        private String name;
    }

    @Data
    public static class Sample {
        private String alt;
        private String id;
        private String src;
        private String thumbnail;
    }

    @Data
    static class SimilarMovie {
        private String id;
        private String title;
        private String img;
    }
}
