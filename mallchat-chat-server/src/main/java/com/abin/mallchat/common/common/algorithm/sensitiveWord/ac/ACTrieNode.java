package com.abin.mallchat.common.common.algorithm.sensitiveWord.ac;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by berg on 2023/6/18.
 */
@Getter
@Setter
public class ACTrieNode {

    // 子节点
    private Map<Character, ACTrieNode> children = Maps.newHashMap();

    // 匹配过程中，如果模式串不匹配，模式串指针会回退到failover继续进行匹配
    private ACTrieNode failover = null;

    private int depth;

    private boolean isLeaf = false;

    public void addChildrenIfAbsent(char c) {
        children.computeIfAbsent(c, (key) -> new ACTrieNode());
    }

    public ACTrieNode childOf(char c) {
        return children.get(c);
    }

    public boolean hasChild(char c) {
        return children.containsKey(c);
    }

    @Override
    public String toString() {
        return "ACTrieNode{" +
                "failover=" + failover +
                ", depth=" + depth +
                ", isLeaf=" + isLeaf +
                '}';
    }
}
