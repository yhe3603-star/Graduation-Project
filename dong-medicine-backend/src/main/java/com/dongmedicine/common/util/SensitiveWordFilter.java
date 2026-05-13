package com.dongmedicine.common.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class SensitiveWordFilter {

    public enum SensitiveLevel {
        NONE,
        NORMAL,
        HIGH
    }

    private static final String HIGH_DICT_PATH = "sensitive-words-high.txt";
    private static final String NORMAL_DICT_PATH = "sensitive-words.txt";

    private Map<Character, Object> highRoot = new HashMap<>();
    private Map<Character, Object> normalRoot = new HashMap<>();

    @PostConstruct
    public void init() {
        int highCount = loadDictionary(HIGH_DICT_PATH, highRoot);
        int normalCount = loadDictionary(NORMAL_DICT_PATH, normalRoot);
        log.info("敏感词词库加载完成，高危 {} 个，普通 {} 个", highCount, normalCount);
    }

    private int loadDictionary(String path, Map<Character, Object> root) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            String word;
            while ((word = reader.readLine()) != null) {
                word = word.trim();
                if (!word.isEmpty()) {
                    addWord(word.toLowerCase(), root);
                    count++;
                }
            }
        } catch (Exception e) {
            log.warn("敏感词词库加载失败 [{}]: {}", path, e.getMessage());
        }
        return count;
    }

    private void addWord(String word, Map<Character, Object> root) {
        Map<Character, Object> currentMap = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object node = currentMap.get(c);
            if (node == null) {
                Map<Character, Object> newNode = new HashMap<>();
                currentMap.put(c, newNode);
                currentMap = newNode;
            } else {
                currentMap = (Map<Character, Object>) node;
            }
        }
        currentMap.put('\0', null);
    }

    public SensitiveLevel matchLevel(String text) {
        if (!StringUtils.hasText(text)) {
            return SensitiveLevel.NONE;
        }
        if (containsIn(text.toLowerCase(), highRoot)) {
            return SensitiveLevel.HIGH;
        }
        if (containsIn(text.toLowerCase(), normalRoot)) {
            return SensitiveLevel.NORMAL;
        }
        return SensitiveLevel.NONE;
    }

    public List<String> match(String text) {
        List<String> matchedWords = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return matchedWords;
        }
        text = text.toLowerCase();
        matchIn(text, highRoot, matchedWords);
        matchIn(text, normalRoot, matchedWords);
        return matchedWords;
    }

    public boolean containsSensitiveWord(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String lower = text.toLowerCase();
        return containsIn(lower, highRoot) || containsIn(lower, normalRoot);
    }

    private boolean containsIn(String text, Map<Character, Object> root) {
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> currentMap = root;
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                Object node = currentMap.get(c);
                if (node == null) {
                    break;
                }
                currentMap = (Map<Character, Object>) node;
                if (currentMap.containsKey('\0')) {
                    return true;
                }
            }
        }
        return false;
    }

    private void matchIn(String text, Map<Character, Object> root, List<String> result) {
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> currentMap = root;
            int end = -1;
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                Object node = currentMap.get(c);
                if (node == null) {
                    break;
                }
                currentMap = (Map<Character, Object>) node;
                if (currentMap.containsKey('\0')) {
                    end = j;
                }
            }
            if (end >= 0) {
                result.add(text.substring(i, end + 1));
            }
        }
    }
}
