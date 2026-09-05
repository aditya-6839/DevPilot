package com.devPilot.backend.github.service.impl;

import com.devPilot.backend.github.service.GithubApiClient;
import com.devPilot.backend.repository.dto.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GithubApiClientImpl implements GithubApiClient {

    private static final String API_BASE = "https://api.github.com";

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 10;

    private static final ParameterizedTypeReference<List<GithubRepository>> GITHUB_REPOSITORY_LIST =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<Map<String, Object>> MAP =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient.Builder restClientBuilder;

    private static void requireNonBlank(String value , String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    /**
     * Creates a GitHub API client configured with the user's access token.
     */
    private RestClient client(String accessToken) {
        requireNonBlank(accessToken , "GitHub access token");

        return restClientBuilder
                .baseUrl(API_BASE)
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(accessToken);
                    headers.set(
                            HttpHeaders.ACCEPT ,
                            "application/vnd.github+json"
                    );
                    headers.set(
                            "X-GitHub-Api-Version" ,
                            "2022-11-28"
                    );
                    headers.set(
                            HttpHeaders.USER_AGENT ,
                            "DevPilot"
                    );
                })
                .build();
    }

    @Override
    public List<GithubRepository> listUserRepos(String accessToken) {
        RestClient client = client(accessToken);

        List<GithubRepository> repositories = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES; page++) {

            final int currentPage = page;

            List<GithubRepository> pageRepos = client
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/repos")
                            .queryParam(
                                    "affiliation" ,
                                    "owner,collaborator,organization_member"
                            )
                            .queryParam("sort" , "updated")
                            .queryParam("per_page" , PAGE_SIZE)
                            .queryParam("page" , currentPage)
                            .build())
                    .retrieve()
                    .body(GITHUB_REPOSITORY_LIST);

            if (pageRepos == null || pageRepos.isEmpty()) {
                break;
            }

            repositories.addAll(pageRepos);

            if (pageRepos.size() < PAGE_SIZE) {
                break;
            }
        }

        return repositories;
    }


    @Override
    public Map<String, Object> getRepoTree(
            String accessToken ,
            String owner ,
            String repo ,
            String branch
    ) {
        requireNonBlank(owner , "Repository owner");
        requireNonBlank(repo , "Repository name");
        requireNonBlank(branch , "Branch");

        return client(accessToken)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/git/trees/{branch}")
                        .queryParam("recursive" , "1")
                        .build(owner , repo , branch))
                .retrieve()
                .body(MAP);
    }

    @Override
    public String getFileContent(
            String accessToken ,
            String owner ,
            String repo ,
            String path
    ) {
        requireNonBlank(owner , "Repository owner");
        requireNonBlank(repo , "Repository name");
        requireNonBlank(path , "File path");

        Map<String, Object> body = client(accessToken)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/contents/{path}")
                        .build(owner , repo , path))
                .retrieve()
                .body(MAP);

        if (body == null) {
            return null;
        }

        Object content = body.get("content");

        if (content == null) {
            return null;
        }

        String encoding = String.valueOf(body.get("encoding"));
        String contentValue = String.valueOf(content);

        if ("base64".equalsIgnoreCase(encoding)) {
            return decodeBase64(contentValue);
        }

        return contentValue;
    }

    private String decodeBase64(String content) {
        try {
            String normalized = content.replaceAll("\\s+" , "");

            byte[] decoded = Base64.getDecoder().decode(normalized);

            return new String(
                    decoded ,
                    StandardCharsets.UTF_8
            );

        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "GitHub returned invalid Base64 file content" ,
                    ex
            );
        }
    }
}

