package cn.devops.jira.config;

import cn.devops.jira.service.JiraService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloseBean implements DisposableBean {

    final JiraService jiraService;

    public CloseBean(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @Override
    public void destroy() throws Exception {
        jiraService.disposeHttp();
    }
}
