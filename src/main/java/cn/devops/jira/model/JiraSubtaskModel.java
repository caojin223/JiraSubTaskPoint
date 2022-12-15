package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraSubtaskModel {
    String id;
    String key;
    String summary;
    String type = "subtask";
    String status;
    int point;
}
