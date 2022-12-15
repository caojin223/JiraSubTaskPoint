package cn.devops.jira;

import cn.devops.jira.model.JiraProjectModel;
import cn.devops.jira.model.JiraSprintModel;
import cn.devops.jira.model.JiraSubtaskModel;
import cn.devops.jira.service.JiraService;
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
            List<JiraSprintModel> sprints = jiraService.getSprintByProject(project.getKey());

            for (JiraSprintModel sprint : sprints) {
                List<JiraSubtaskModel> tasks = jiraService.getSubtaskBySprint(sprint.getId());
                for (JiraSubtaskModel task : tasks) {
                    log.info(task.toString());
                }
            }
        }
    }
}
