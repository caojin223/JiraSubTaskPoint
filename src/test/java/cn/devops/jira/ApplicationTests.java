package cn.devops.jira;

import cn.devops.jira.model.JiraProjectModel;
import cn.devops.jira.model.JiraSprintModel;
import cn.devops.jira.model.JiraSprintResModel;
import cn.devops.jira.service.JiraService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class ApplicationTests {

    @Autowired
    JiraService jiraService;

    @Test
    void testPrintAllSubTasksByProjectKey() {
        List<JiraProjectModel> projects = jiraService.getProjects();
        Assert.isTrue(projects.size() > 0, "项目不能为空");
        for (JiraProjectModel project : projects) {
            List<JiraSprintResModel> sprints = jiraService.getSubtaskByProjectKey(project.getKey());
            log.info("Project name: {}, json: {}", project.getName(), JSONObject.toJSONString(sprints));
        }
    }

    @Test
    void testPrintAllSubTasksBySprintIds() {
        List<JiraProjectModel> projects = jiraService.getProjects();
        Assert.isTrue(projects.size() > 0, "项目不能为空");
        for (JiraProjectModel project : projects) {
            List<JiraSprintModel> sprints = jiraService.getSprintByProjectKey(project.getKey());
            List<Integer> sprintIds = sprints.stream().map(JiraSprintModel::getId).collect(Collectors.toList());
            List<JiraSprintResModel> sprintRes = jiraService.getSubtaskBySprintIds(sprintIds);
            log.info("Project name: {}, json: {}", project.getName(), JSONObject.toJSONString(sprintRes));
        }
    }
}
