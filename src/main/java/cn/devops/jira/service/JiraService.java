package cn.devops.jira.service;

import cn.devops.jira.model.JiraProjectModel;
import cn.devops.jira.model.JiraSprintModel;
import cn.devops.jira.model.JiraSubtaskModel;

import java.util.List;

/**
 * @author caojin***************
 */
public interface JiraService {

    List<JiraProjectModel> getProjects();

    List<JiraSprintModel> getSprintByProject(String project);

    List<JiraSubtaskModel> getSubtaskBySprint(int sprintId);

    List<JiraSubtaskModel> getSubtaskByProject(String project);

    void disposeHttp();
}
