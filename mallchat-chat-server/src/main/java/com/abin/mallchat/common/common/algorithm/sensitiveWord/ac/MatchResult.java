package com.abin.mallchat.common.common.algorithm.sensitiveWord.ac;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by berg on 2023/6/18.
 */
@Getter
@Setter
@AllArgsConstructor
public class MatchResult {

    private int startIndex;

    private int endIndex;

    @Override
    public String toString() {
        return "MatchResult{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }
}
