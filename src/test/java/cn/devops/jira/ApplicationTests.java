package cn.devops.jira;

import cn.devops.jira.model.JiraProjectModel;
import cn.devops.jira.model.JiraSprintResModel;
import cn.devops.jira.service.JiraService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@SpringBootTest
class ApplicationTests {

    @Autowired
    JiraService jiraService;

    @Test
    void testPrintAllSubTasks() {
        List<JiraProjectModel> projects = jiraService.getProjects();
        Assert.isTrue(projects.size() > 0, "项目不能为空");
        for (JiraProjectModel project : projects) {
            List<JiraSprintResModel> sprints = jiraService.getSubtaskByProjectKey(project.getKey());
            log.info("Project name: {}, json: {}", project.getName(), JSONObject.toJSONString(sprints));
        }
    }
}
