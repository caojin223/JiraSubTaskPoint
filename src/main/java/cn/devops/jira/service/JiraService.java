package cn.devops.jira.service;

import cn.devops.jira.model.JiraProjectModel;
import cn.devops.jira.model.JiraSprintModel;
import cn.devops.jira.model.JiraSprintResModel;

import java.util.List;

/**
 * @author caojin***************
 */
public interface JiraService {

    List<JiraProjectModel> getProjects();

    List<JiraSprintModel> getSprintByProjectKey(String projectKey);

    List<JiraSprintResModel> getSubtaskByProjectKey(String projectKey);

    List<JiraSprintResModel> getSubtaskBySprintIds(List<Integer> sprintIds);

    void disposeHttp();
}
