package com.joxad.zikobot.data.module.lyrics.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class LyricResult {

    @SerializedName("result")
    @Expose
    private Result result;

}