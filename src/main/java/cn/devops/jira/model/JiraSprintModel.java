package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class JiraSprintModel {
    Integer id;
    String state;
    String name;
    Date startDate;
    Date endDate;
//    int originBoardId;
}
