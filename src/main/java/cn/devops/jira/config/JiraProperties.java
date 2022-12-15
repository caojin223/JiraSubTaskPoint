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
     * jira subTask，用于表示工时的自定义的字段名
     */
    String pointName;

    /**
     * jira获取项目列表的api地址
     */
    String apiProject;

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

    String jqlQuery;
}
