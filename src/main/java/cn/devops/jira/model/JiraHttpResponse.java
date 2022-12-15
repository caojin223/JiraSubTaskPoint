package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraHttpResponse {
    boolean success = false;
    String exception = "";
    String body = "";
}
