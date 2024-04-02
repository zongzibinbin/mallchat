package com.abin.mallchat.common.common.algorithm.sensitiveWord;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 敏感词工具类
 *
 * @author zhaoyuhang
 * @date 2023/06/19
 */
public final class DFAFilter implements SensitiveWordFilter {

    private DFAFilter() {
    }

    private static Word root = new Word(' '); // 敏感词字典的根节点
    private final static char replace = '*'; // 替代字符
    private final static String skipChars = " !*-+_=,，.@;:；：。、？?（）()【】[]《》<>“”\"‘’"; // 遇到这些字符就会跳过
    private final static Set<Character> skipSet = new HashSet<>(); // 遇到这些字符就会跳过

    static {
        for (char c : skipChars.toCharArray()) {
            skipSet.add(c);
        }
    }

    public static DFAFilter getInstance() {
        return new DFAFilter();
    }


    /**
     * 判断文本中是否存在敏感词
     *
     * @param text 文本
     * @return true: 存在敏感词, false: 不存在敏感词
     */
    public boolean hasSensitiveWord(String text) {
        if (StringUtils.isBlank(text)) return false;
        return !Objects.equals(filter(text), text);
    }

    /**
     * 敏感词替换
     *
     * @param text 待替换文本
     * @return 替换后的文本
     */
    public String filter(String text) {
        StringBuilder result = new StringBuilder(text);
        int index = 0;
        while (index < result.length()) {
            char c = result.charAt(index);
            if (skip(c)) {
                index++;
                continue;
            }
            Word word = root;
            int start = index;
            boolean found = false;
            for (int i = index; i < result.length(); i++) {
                c = result.charAt(i);
                if (skip(c)) {
                    continue;
                }
                if (c >= 'A' && c <= 'Z') {
                    c += 32;
                }
                word = word.next.get(c);
                if (word == null) {
                    break;
                }
                if (word.end) {
                    found = true;
                    for (int j = start; j <= i; j++) {
                        result.setCharAt(j, replace);
                    }
                    index = i;
                }
            }
            if (!found) {
                index++;
            }
        }
        return result.toString();
    }


    /**
     * 加载敏感词列表
     *
     * @param words 敏感词数组
     */
    public void loadWord(List<String> words) {
        if (!CollectionUtils.isEmpty(words)) {
            Word newRoot = new Word(' ');
            words.forEach(word -> loadWord(word, newRoot));
            root = newRoot;
        }
    }

    /**
     * 加载敏感词
     *
     * @param word 词
     */
    public void loadWord(String word, Word root) {
        if (StringUtils.isBlank(word)) {
            return;
        }
        Word current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            // 如果是大写字母, 转换为小写
            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }
            if (skip(c)) {
                continue;
            }
            Word next = current.next.get(c);
            if (next == null) {
                next = new Word(c);
                current.next.put(c, next);
            }
            current = next;
        }
        current.end = true;
    }


    /**
     * 从文本文件中加载敏感词列表
     *
     * @param path 文本文件的绝对路径
     */
    public void loadWordFromFile(String path) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
            loadWord(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从流中加载敏感词列表
     *
     * @param inputStream 文本文件输入流
     * @throws IOException IO异常
     */
    public void loadWord(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            ArrayList<String> list = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            loadWord(list);
        }
    }

    /**
     * 判断是否需要跳过当前字符
     *
     * @param c 待检测字符
     * @return true: 需要跳过, false: 不需要跳过
     */
    private boolean skip(char c) {
        return skipSet.contains(c);
    }

    /**
     * 敏感词类
     */
    private static class Word {
        // 当前字符
        private final char c;

        // 结束标识
        private boolean end;

        // 下一层级的敏感词字典
        private Map<Character, Word> next;

        public Word(char c) {
            this.c = c;
            this.next = new HashMap<>();
        }
    }
}
