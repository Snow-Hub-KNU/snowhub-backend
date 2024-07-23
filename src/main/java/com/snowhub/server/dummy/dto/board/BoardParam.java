package com.snowhub.server.dummy.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class BoardParam {

    // 오류 감지기
    @NotBlank(message = "Title is Empty.")
    private String title;
    @NotBlank(message = "Content is Empty.")
    private String content;
    @NotBlank(message = "Choose Category.")
    private String category;
}
