package com.abin.mallchat.common.common.algorithm.sensitiveWord.acpro;

import java.util.*;

/**
 * @author CtrlCver
 * @date 2024/1/12
 * @description: AC自动机
 */
public class ACProTrie {

    private final static char MASK = '*'; // 替代字符

    private Word root;

    // 节点
    static class Word {
        // 判断是否是敏感词结尾
        boolean end = false;
        // 失败回调节点/状态
        Word failOver = null;
        // 记录字符偏移
        int depth = 0;
        // 下个自动机状态
        Map<Character, Word> next = new HashMap<>();

        public boolean hasChild(char c) {
            return next.containsKey(c);
        }
    }

    //构建ACTrie
    public void createACTrie(List<String> list) {
        Word currentNode = new Word();
        root = currentNode;
        for (String key : list) {
            currentNode = root;
            for (int j = 0; j < key.length(); j++) {
                if (currentNode.next != null && currentNode.next.containsKey(key.charAt(j))) {
                    currentNode = currentNode.next.get(key.charAt(j));
                } else {
                    Word childNode = new Word();
                    currentNode.next.put(key.charAt(j), childNode);
                    currentNode = childNode;
                }
                currentNode.depth = j + 1;
            }
            //每个敏感词遍历结束后，再设置结尾标识
            currentNode.end = true;
        }
        initFailOver();
    }

    // 初始化匹配失败回调节点/状态
    public void initFailOver() {
        Queue<Word> queue = new LinkedList<>();
        Map<Character, Word> children = root.next;
        for (Word node : children.values()) {
            node.failOver = root;
            queue.offer(node);
        }
        while (!queue.isEmpty()) {
            Word parentNode = queue.poll();
            for (Map.Entry<Character, Word> entry : parentNode.next.entrySet()) {
                Word childNode = entry.getValue();
                Word failOver = parentNode.failOver;
                while (failOver != null && (!failOver.next.containsKey(entry.getKey()))) {
                    failOver = failOver.failOver;
                }
                if (failOver == null) {
                    childNode.failOver = root;
                } else {
                    childNode.failOver = failOver.next.get(entry.getKey());
                }
                queue.offer(childNode);
            }
        }
    }

    // 匹配
    public String match(String matchWord) {
        Word walkNode = root;
        char[] wordArray = matchWord.toCharArray();
        for (int i = 0; i < wordArray.length; i++) {
            // 失败"回溯"
            while (!walkNode.hasChild(wordArray[i]) && walkNode.failOver != null) {
                walkNode = walkNode.failOver;
            }
            if (walkNode.hasChild(wordArray[i])) {
                walkNode = walkNode.next.get(wordArray[i]);
                if (walkNode.end) {
                    // sentinelA和sentinelB作为哨兵节点，去后面探测是否仍存在end
                    Word sentinelA = walkNode; // 记录当前节点
                    Word sentinelB = walkNode; //记录end节点
                    int k = i + 1;
                    //判断end是不是最终end即敏感词是否存在包含关系(abc,abcd)
                    while (k < wordArray.length && sentinelA.hasChild(wordArray[k])) {
                        sentinelA = sentinelA.next.get(wordArray[k]);
                        k++;
                        if (sentinelA.end) {
                            sentinelB = sentinelA;
                        }
                    }
                    // 计算敏感词总长度,不需要判断flag,如果未匹配成功,则sentinelB = walkNode
                    int len = sentinelB.depth;
                    // 此刻的i是第一个敏感词的最后一个字符下标，减去第一个敏感词的长度再加一即得到开始下标
                    int startIndex = i - walkNode.depth + 1;
                    int endIndex = startIndex + len - 1;
                    // 遍历替换成 MASK
                    while (startIndex <= endIndex && startIndex < wordArray.length) {
                        wordArray[startIndex++] = MASK;
                    }
                    // 此刻的i是第一个敏感词的最后一个字符下标，所以在加上总长度后需要再减去第一个敏感词的长度
                    // 不需要判断flag标识，如果为false，sentinelB长度就等于walkNode长度
                    i += sentinelB.depth - walkNode.depth;
                    // 更新node
                    walkNode = sentinelB.failOver;
                }
            }
        }
        return new String(wordArray);
    }
}
