package com.devhub.source.api;

import com.devhub.source.app.SourceFinder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceFinder finder;

    @GetMapping
    public List<SourceResponse> findSources() {
        return finder.findEnabled().stream().map(SourceResponse::from).toList();
    }
}