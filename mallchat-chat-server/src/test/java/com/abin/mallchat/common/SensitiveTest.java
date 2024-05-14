package com.abin.mallchat.common;

import com.abin.mallchat.common.common.algorithm.sensitiveWord.ACFilter;
import com.abin.mallchat.common.common.algorithm.sensitiveWord.ACProFilter;
import com.abin.mallchat.common.common.algorithm.sensitiveWord.DFAFilter;
import com.abin.mallchat.common.common.algorithm.sensitiveWord.SensitiveWordFilter;
import org.junit.Test;
import java.lang.reflect.Constructor;
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
    public void ACPro()throws Exception {
        // 结果：*****
        System.out.println(testUtil(Arrays.asList("白痴", "你是白痴", "白痴吗"), "你是白痴吗", ACProFilter.class));
        // 结果：*******
        System.out.println(testUtil(Arrays.asList("白痴", "白痴吗你是"), "白痴吗你是白痴", ACProFilter.class));
        // 结果：*****小**明
        System.out.println(testUtil(Arrays.asList("白痴", "白痴吗你是"), "白痴吗你是小白痴明", ACProFilter.class));
    }
    public String testUtil(List<String> sensitiveList, String pattern, Class<? extends SensitiveWordFilter> clazz) throws Exception {
        Constructor constructor = clazz.getConstructor(); // 获取无参构造
        SensitiveWordFilter wordFilter = (SensitiveWordFilter)constructor.newInstance();
        wordFilter.loadWord(sensitiveList);
        return wordFilter.filter(pattern);
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
