package cn.devops.jira.controller;

import cn.devops.jira.response.ResponseHandler;
import cn.devops.jira.response.ResponseResult;
import cn.devops.jira.service.JiraService;
import cn.devops.jira.util.TimeTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class JiraController {

    final JiraService jiraService;

    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @GetMapping("projects")
    public ResponseResult<?> getProjects() {
        log.info("getProjects");
        return ResponseHandler.success(TimeTools.now(), jiraService.getProjects());
    }

    @GetMapping("sprint")
    public ResponseResult<?> getSprintByProjectKey(
            @RequestParam(name = "projectKey") String projectKey) {
        log.info("getSprintByProjectKey: {}", projectKey);
        return ResponseHandler.success(TimeTools.now(), jiraService.getSprintByProjectKey(projectKey));
    }

    @GetMapping("subtask")
    public ResponseResult<?> getSubtaskByProjectKey(
            @RequestParam(name = "projectKey", required = false) String projectKey,
            @RequestParam(name = "sprintIds", required = false) List<Integer> sprintIds
    ) {
        if (sprintIds != null && !sprintIds.isEmpty()) {
            log.info("getSubtaskBySprintIds: {}", sprintIds);
            return ResponseHandler.success(TimeTools.now(), jiraService.getSubtaskBySprintIds(sprintIds));
        } else if (projectKey != null) {
            log.info("getSubtaskByProjectKey: {}", projectKey);
            return ResponseHandler.success(TimeTools.now(), jiraService.getSubtaskByProjectKey(projectKey));
        } else {
            log.error("getSubtask ???????????? projectKey ?????? SprintIds");
            return ResponseHandler.error(TimeTools.now(), null);
        }

    }

    @GetMapping("health")
    public boolean checkHealth() {
        return true;
    }
}
