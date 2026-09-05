package com.devPilot.backend.repository.service.impl;


import com.devPilot.backend.User.entity.User;
import com.devPilot.backend.User.service.UserService;
import com.devPilot.backend.exception.NotFoundException;
import com.devPilot.backend.github.service.GithubApiClient;
import com.devPilot.backend.repository.dto.GithubRepository;
import com.devPilot.backend.repository.dto.IndexStatusResponse;
import com.devPilot.backend.repository.dto.RepositoryResponse;
import com.devPilot.backend.repository.entity.Repository;
import com.devPilot.backend.repository.repository.RepositoryRepository;
import com.devPilot.backend.repository.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RepoServiceImpl implements RepoService {
    private final RepositoryRepository repositoryRepository;
    private final UserService userService;
    private final GithubApiClient githubApiClient;

    private static String getString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("GitHub repository ID must not be null");
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    @Override
    public List<RepositoryResponse> listStored(UUID userId) {
        return repositoryRepository.findByUserIdOrderByFullNameAsc(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public List<RepositoryResponse> syncAndListRepos(UUID userId) {

        User user = userService.requiredById(userId);

        String accessToken = userService.getDecryptedAccessToken(user);

        List<GithubRepository> remoteRepos =
                githubApiClient.listUserRepos(accessToken);

        List<Repository> savedRepositories = new ArrayList<>();

        for (GithubRepository remote : remoteRepos) {

            Long githubRepoId = remote.getId();

            Repository repository = repositoryRepository
                    .findByUserIdAndGithubRepoId(userId , githubRepoId)
                    .orElseGet(Repository::new);

            repository.setUserId(userId);
            repository.setGithubRepoId(githubRepoId);

            repository.setName(remote.getName());
            repository.setFullName(remote.getFullName());

            /*
             * GitHub owner
             */
            if (remote.getOwner() != null) {
                repository.setOwner(remote.getOwner().getLogin());
            }

            /*
             * Repository information
             */
            repository.setPrivate(
                    Boolean.TRUE.equals(remote.getIsPrivate())
            );

            repository.setDefaultBranch(
                    remote.getDefaultBranch() != null
                            ? remote.getDefaultBranch()
                            : "main"
            );

            repository.setLanguage(remote.getLanguage());
            repository.setHtmlUrl(remote.getHtmlUrl());
            repository.setDescription(remote.getDescription());

            /*
             * Local sync timestamp
             */
            repository.setUpdatedAt(Instant.now());

            savedRepositories.add(
                    repositoryRepository.save(repository)
            );
        }

        return savedRepositories.stream()
                .sorted(
                        (a , b) -> a.getFullName()
                                .compareToIgnoreCase(b.getFullName())
                )
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Repository requireOwned(UUID repoId , UUID userId) {
        return repositoryRepository.findByIdAndUserId(repoId , userId).orElseThrow(() -> new NotFoundException("Repository not found"));
    }

    @Override
    public IndexStatusResponse status(UUID repoId , UUID userId) {
        Repository repo = requireOwned(repoId , userId);
        return new IndexStatusResponse(repo.getId() , repo.getIndexStatus() , repo.getFilesTotal() , repo.getFilesProcessed() , repo.getChunkCount() , repo.getIndexedAt() , repo.getErrorMessage());
    }

    @Override
    public RepositoryResponse toResponse(Repository repo) {
        return new RepositoryResponse(repo.getId() , repo.getGithubRepoId() , repo.getOwner() , repo.getName() , repo.getFullName() , repo.isPrivate() , repo.getDefaultBranch() , repo.getLanguage() , repo.getHtmlUrl() , repo.getDescription() , repo.getIndexStatus() , repo.getIndexedAt() , repo.getChunkCount() , repo.getFilesTotal() , repo.getFilesProcessed() , repo.getErrorMessage());
    }
}