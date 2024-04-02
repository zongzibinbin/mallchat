package com.abin.mallchat.common.common.algorithm.sensitiveWord.ac;

import com.google.common.collect.Lists;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * aho-corasick算法（又称AC自动机算法）
 * Created by berg on 2023/6/18.
 */
@NotThreadSafe
public class ACTrie {

    // 根节点
    private ACTrieNode root;

    public ACTrie(List<String> words) {
        words = words.stream().distinct().collect(Collectors.toList()); // 去重
        root = new ACTrieNode();
        for (String word : words) {
            addWord(word);
        }
        initFailover();
    }

    public void addWord(String word) {
        ACTrieNode walkNode = root;
        char[] chars = word.toCharArray();
        for (int i = 0; i < word.length(); i++) {
            walkNode.addChildrenIfAbsent(chars[i]);
            walkNode = walkNode.childOf(chars[i]);
            walkNode.setDepth(i + 1);
        }
        walkNode.setLeaf(true);
    }

    /**
     * 初始化节点中的回退指针
     */
    private void initFailover() {
        //第一层的fail指针指向root
        Queue<ACTrieNode> queue = new LinkedList<>();
        Map<Character, ACTrieNode> children = root.getChildren();
        for (ACTrieNode node : children.values()) {
            node.setFailover(root);
            queue.offer(node);
        }
        //构建剩余层数节点的fail指针,利用层次遍历
        while (!queue.isEmpty()) {
            ACTrieNode parentNode = queue.poll();
            for (Map.Entry<Character, ACTrieNode> entry : parentNode.getChildren().entrySet()) {
                ACTrieNode childNode = entry.getValue();
                ACTrieNode failover = parentNode.getFailover();
                // 在树中找到以childNode为结尾的字符串的最长前缀匹配，failover指向了这个最长前缀匹配的父节点
                while (failover != null && (!failover.hasChild(entry.getKey()))) {
                    failover = failover.getFailover();
                }
                //回溯到了root节点
                if (failover == null) {
                    childNode.setFailover(root);
                } else {
                    // 更新当前节点的回退指针
                    childNode.setFailover(failover.childOf(entry.getKey()));
                }
                queue.offer(childNode);
            }
        }
    }

    /**
     * 查询句子中包含的敏感词的起始位置和结束位置
     *
     * @param text
     */
    public List<MatchResult> matches(String text) {
        List<MatchResult> result = Lists.newArrayList();
        ACTrieNode walkNode = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (!walkNode.hasChild(c) && walkNode.getFailover() != null) {
                walkNode = walkNode.getFailover();
            }
            //如果因为当前节点的孩子节点有这个字符，则将walkNode替换为下面的孩子节点
            if (walkNode.hasChild(c)) {
                walkNode = walkNode.childOf(c);
                // 检索到了敏感词
                if (walkNode.isLeaf()) {
                    result.add(new MatchResult(i - walkNode.getDepth() + 1, i + 1));
                    // 模式串回退到最长可匹配前缀位置并开启新一轮的匹配
                    // 这种回退方式将一个不漏的匹配到所有的敏感词，匹配结果的区间可能会有重叠的部分
                    walkNode = walkNode.getFailover();
                }
            }
        }
        return result;
    }

}
