package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class JiraSprintModel {
    int id;
    String state;
    String name;
    Date startDate;
    Date endDate;
    int originBoardId;
    List<JiraSubtaskModel> subtasks;
}
