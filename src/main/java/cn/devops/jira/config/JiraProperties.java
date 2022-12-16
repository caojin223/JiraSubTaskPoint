package cn.devops.jira.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author caojin
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jira")
public class JiraProperties {
    /**
     * jira的域名地址
     */
    String host;
    /**
     * jira的管理员用户名
     */
    String username;
    /**
     * jira的管理员密码
     */
    String password;

    /**
     * jira subTask的custom field point的名称，用于匹配id，如：customfield_10013
     */
    String pointName;

    /**
     * jira custom field Sprint的名称，用于匹配id，如：customfield_10007
     */
    String sprintName;

    /**
     * jira获取项目列表的api地址
     */
    String apiProject;

    /**
     * jira获取所有Field
     */
    String apiField;

    /**
     * jira获取项目看板的api地址
     */
    String apiBoards;

    /**
     * jira获取Sprint的api地址
     */
    String apiSprint;

    /**
     * jira查询jql的api地址
     */
    String jql;

//    /**
//     * jira查询subtask的jql
//     */
//    String jqlBySprint;

    /**
     * jira查询subtask的jql
     */
    String jqlByProject;
}
