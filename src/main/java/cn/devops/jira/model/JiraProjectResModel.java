package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class JiraProjectResModel extends JiraProjectModel {

    List<JiraSprintResModel> sprints = new ArrayList<>();

}
