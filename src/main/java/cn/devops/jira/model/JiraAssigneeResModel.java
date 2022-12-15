package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class JiraAssigneeResModel {
    String name;
    String key;
    String emailAddress;
    String avatarUrl;
    String displayName = "未分配";
    boolean active = false;
    int pointCount = 0;
    List<JiraSubtaskModel> subtasks = new ArrayList<>();
}
