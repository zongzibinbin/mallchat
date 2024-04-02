package com.abin.mallchat.common.common.algorithm.sensitiveWord;

import java.util.List;

/**
 * 敏感词引导类
 *
 * @author zhaoyuhang
 * @date 2023/07/08
 */
public class SensitiveWordBs {

    /**
     * 私有化构造器
     */
    private SensitiveWordBs() {
    }

    /**
     * 脱敏策略
     */
    private SensitiveWordFilter sensitiveWordFilter = DFAFilter.getInstance();

    /**
     * 敏感词列表
     */
    private IWordFactory wordDeny;

    public static SensitiveWordBs newInstance() {
        return new SensitiveWordBs();
    }

    /**
     * 初始化
     * <p>
     * 1. 根据配置，初始化对应的 map。比较消耗性能。
     *
     * @return this
     * @since 0.0.13
     */
    public SensitiveWordBs init() {

        List<String> words = wordDeny.getWordList();
        loadWord(words);
        return this;
    }

    /**
     * 过滤策略
     *
     * @param filter 过滤器
     * @return 结果
     * @since 0.7.0
     */
    public SensitiveWordBs filterStrategy(SensitiveWordFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter can not be null");
        }
        this.sensitiveWordFilter = filter;
        return this;
    }

    public SensitiveWordBs sensitiveWord(IWordFactory wordFactory) {
        if (wordFactory == null) {
            throw new IllegalArgumentException("wordFactory can not be null");
        }
        this.wordDeny = wordFactory;
        return this;
    }


    /**
     * 有敏感词
     *
     * @param text 文本
     * @return boolean
     */
    public boolean hasSensitiveWord(String text) {
        return sensitiveWordFilter.hasSensitiveWord(text);
    }

    /**
     * 过滤
     *
     * @param text 文本
     * @return {@link String}
     */
    public String filter(String text) {
        return sensitiveWordFilter.filter(text);
    }

    /**
     * 加载敏感词列表
     *
     * @param words 敏感词数组
     */
    private void loadWord(List<String> words) {
        sensitiveWordFilter.loadWord(words);
    }

}
