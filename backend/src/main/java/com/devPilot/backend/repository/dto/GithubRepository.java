package com.devPilot.backend.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubRepository {

    private Long id;

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("private")
    private Boolean isPrivate;

    @JsonProperty("default_branch")
    private String defaultBranch;

    private String language;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String description;

    private GithubOwner owner;
}
