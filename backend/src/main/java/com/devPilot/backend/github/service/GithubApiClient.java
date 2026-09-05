package com.devPilot.backend.github.service;

import com.devPilot.backend.repository.dto.GithubRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface GithubApiClient {

    List<GithubRepository> listUserRepos(String accessToken);

    Map<String, Object> getRepoTree(
            String accessToken,
            String owner,
            String repo,
            String branch
    );

    String getFileContent(
            String accessToken,
            String owner,
            String repo,
            String path
    );
}