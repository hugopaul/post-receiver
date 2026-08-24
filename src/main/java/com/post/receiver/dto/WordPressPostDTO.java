package com.post.receiver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordPressPostDTO {
    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private String author;
    private String date;
    private String status;
    private String slug;
    private String type;
    private String link;
}

