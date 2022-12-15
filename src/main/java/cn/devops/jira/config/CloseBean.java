package cn.devops.jira.config;

import cn.devops.jira.service.JiraService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloseBean implements DisposableBean {

    @Autowired
    JiraService jiraService;

    @Override
    public void destroy() throws Exception {
        jiraService.disposeHttp();
    }
}
