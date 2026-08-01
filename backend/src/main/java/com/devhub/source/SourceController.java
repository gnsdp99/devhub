package com.devhub.source;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceRepository repository;

    @GetMapping
    public List<SourceResponse> findSources() {
        return repository.findEnabled().stream().map(SourceResponse::from).toList();
    }
}