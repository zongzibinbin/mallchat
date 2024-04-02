package com.abin.mallchat.common;

import com.abin.mallchat.common.common.algorithm.sensitiveWord.ACFilter;
import com.abin.mallchat.common.common.algorithm.sensitiveWord.ACProFilter;
import com.abin.mallchat.common.common.algorithm.sensitiveWord.DFAFilter;
import org.junit.Test;
import java.util.*;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-10-08
 */
public class SensitiveTest {
    @Test
    public void DFA() {
        List<String> sensitiveList = Arrays.asList("abcd", "abcbba", "adabca");
        DFAFilter instance = DFAFilter.getInstance();
        instance.loadWord(sensitiveList);
        System.out.println(instance.hasSensitiveWord("adabcd"));
    }


    @Test
    public void AC() {
        List<String> sensitiveList = Arrays.asList("abcd", "abcbba", "adabca");
        ACFilter instance = new ACFilter();
        instance.loadWord(sensitiveList);
        instance.hasSensitiveWord("adabcd");
    }

    @Test
    public void ACPro()
    {
        List<String> sensitiveList = Arrays.asList("白痴", "你是白痴", "白痴吗");
        ACProFilter acProFilter=new ACProFilter();
        acProFilter.loadWord(sensitiveList);
        System.out.println(acProFilter.filter("你是白痴吗"));
    }
    @Test
    public void DFAMulti() {
        List<String> sensitiveList = Arrays.asList("白痴", "你是白痴", "白痴吗");
        DFAFilter instance = DFAFilter.getInstance();
        instance.loadWord(sensitiveList);
        System.out.println(instance.filter("你是白痴吗"));
    }

    @Test
    public void ACMulti() {
        List<String> sensitiveList = Arrays.asList("你是白痴","你是");
        ACFilter instance = new ACFilter();
        instance.loadWord(sensitiveList);
        System.out.println(instance.filter("你是白痴吗"));
    }
}
