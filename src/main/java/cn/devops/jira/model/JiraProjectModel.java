package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JiraProjectModel {
    int id;
    String key;
    String name;
    String projectTypeKey;
    List<JiraSprintModel> sprints;
}
