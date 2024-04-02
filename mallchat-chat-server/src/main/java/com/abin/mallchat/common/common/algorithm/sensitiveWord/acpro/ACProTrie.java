package com.abin.mallchat.common.common.algorithm.sensitiveWord.acpro;

import java.util.*;

/**
 *@author CtrlCver
 *@date 2024/1/12
 *@description:  AC自动机
 */
public class ACProTrie {

    private final static char MASK = '*'; // 替代字符

    private  Word  root;

    // 节点
    static class Word{
        // 判断是否是敏感词结尾
        boolean end=false;
        // 失败回调节点/状态
        Word failOver=null;
        // 记录字符偏移
        int depth=0;
        // 下个自动机状态
        Map<Character,Word> next=new HashMap<>();
        public boolean hasChild(char c) {
            return next.containsKey(c);
        }
    }
    //构建ACTrie
    public   void createACTrie(List<String> list){
        Word currentNode = new Word();
        root=currentNode;
        for(String key : list)
        {
            currentNode=root;
            for(int j=0;j<key.length();j++)
            {
                if(currentNode.next!=null&&currentNode.next.containsKey(key.charAt(j))){
                    currentNode= currentNode.next.get(key.charAt(j));
                    // 防止乱序输入改变end,比如da，dadac，dadac先进入，第二个a为false,da进入后把a设置为true
                    // 这样结果就是a是end，c也是end
                    if(j==key.length()-1){
                        currentNode.end=true;
                    }
                }else {
                    Word map = new Word();
                    if(j==key.length()-1){
                        map.end=true;
                    }
                    currentNode.next.put(key.charAt(j), map);
                    currentNode=map;
                }
                currentNode.depth = j+1;
            }
        }
        initFailOver();
    }
    // 初始化匹配失败回调节点/状态
    public  void initFailOver(){
        Queue<Word> queue=new LinkedList<>();
        Map<Character,Word> children=root.next;
        for(Word node:children.values())
        {
            node.failOver=root;
            queue.offer(node);
        }
        while(!queue.isEmpty())
        {
            Word parentNode=queue.poll();
            for(Map.Entry<Character,Word> entry:parentNode.next.entrySet())
            {
                Word childNode=entry.getValue();
                Word failOver=parentNode.failOver;
                while(failOver!=null&&(!failOver.next.containsKey(entry.getKey()))){
                    failOver=failOver.failOver;
                }
                if(failOver==null){
                    childNode.failOver=root;
                }else{
                    childNode.failOver=failOver.next.get(entry.getKey());
                }
                queue.offer(childNode);
            }
        }
    }
    // 匹配
    public  String match(String matchWord)
    {
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
                    boolean flag = false;
                    //判断end是不是最终end即敏感词是否存在包含关系(abc,abcd)
                    while (k < wordArray.length && sentinelA.hasChild(wordArray[k])) {
                        sentinelA = sentinelA.next.get(wordArray[k]);
                        k++;
                        if (sentinelA.end) {
                            sentinelB = sentinelA;
                            flag = true;
                        }
                    }
                    // 根据结果去替换*
                    // 计算替换长度
                    int len = flag ? sentinelB.depth : walkNode.depth;
                    while (len > 0) {
                        len--;
                        int index = flag ? i - walkNode.depth + 1 + len : i - len;
                        wordArray[index] = MASK;
                    }
                    // 更新i
                    i += flag ? sentinelB.depth : 0;
                    // 更新node
                    walkNode = flag ? sentinelB.failOver : walkNode.failOver;
                }
            }
        }
        return new String(wordArray);
    }
}
