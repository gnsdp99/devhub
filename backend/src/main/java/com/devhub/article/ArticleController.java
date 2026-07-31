package com.devhub.article;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleFinder finder;

    @GetMapping
    public ArticlePage findArticles(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        return finder.findPage(cursor, limit);
    }
}