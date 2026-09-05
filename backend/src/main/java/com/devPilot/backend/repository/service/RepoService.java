package com.devPilot.backend.repository.service;

import com.devPilot.backend.repository.dto.IndexStatusResponse;
import com.devPilot.backend.repository.dto.RepositoryResponse;
import com.devPilot.backend.repository.entity.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface RepoService {
    List<RepositoryResponse> syncAndListRepos(UUID userId);

    List<RepositoryResponse> listStored(UUID userId);

    Repository requireOwned(UUID repoId, UUID userId);

    IndexStatusResponse status(UUID repoId, UUID userId);

    RepositoryResponse toResponse(Repository repo);
}
