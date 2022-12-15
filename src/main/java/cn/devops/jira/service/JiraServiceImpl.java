package cn.devops.jira.service;

import cn.devops.jira.config.JiraProperties;
import cn.devops.jira.model.JiraHttpResponse;
import cn.devops.jira.model.JiraProjectModel;
import cn.devops.jira.model.JiraSprintModel;
import cn.devops.jira.model.JiraSubtaskModel;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.IOException;
import java.util.*;

/**
 * @author caojin
 */
@Slf4j
@Service
public class JiraServiceImpl implements JiraService {


    CloseableHttpClient client = HttpClients.createDefault();

    @Autowired
    JiraProperties jiraProperties;

    @Override
    public void disposeHttp() {
        try {
            client.close();
            log.info("CloseableHttpClient close");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JiraHttpResponse getRequest(String url) {
        JiraHttpResponse rst = new JiraHttpResponse();
        url = url.replace("//", "/");
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        HttpGet getRequest = new HttpGet(jiraProperties.getHost() + url);
        log.info("request url: " + getRequest.getURI());
        getRequest.setHeader("Accept", "application/json");
        getRequest.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
        String auth = Base64Util.encode(jiraProperties.getUsername() + ":" + jiraProperties.getPassword());
        getRequest.setHeader("Authorization", "Basic " + auth);
        try (CloseableHttpResponse response = client.execute(getRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            log.info("httpStatus: " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String body = EntityUtils.toString(entity, "UTF-8");
                    log.info("body length:" + body.length());
                    rst.setBody(body);
                    rst.setSuccess(true);
                }
            }
        } catch (IOException e) {
            log.error("Jira http error", e);
            rst.setException(e.getMessage());
        }
        return rst;
    }

    @Override
    public List<JiraProjectModel> getProjects() {
        List<JiraProjectModel> rst = new ArrayList<>();
        JiraHttpResponse response = getRequest(jiraProperties.getApiProject());
        if (response.isSuccess()) {
            String body = response.getBody();
            JSONArray list = JSONArray.parseArray(body);
            rst.addAll(list.toJavaList(JiraProjectModel.class));
        }
        return rst;
    }

    private List<Integer> getBoardIdByProject(String project) {
        List<Integer> rst = new ArrayList<>();
        JiraHttpResponse response = getRequest(String.format(jiraProperties.getApiBoards(), project));
        if (response.isSuccess()) {
            String body = response.getBody();
            JSONObject json = JSONObject.parseObject(body);
            JSONArray values = json.getJSONArray("values");
            for (int i = 0; i < values.size(); i++) {
                rst.add(values.getJSONObject(i).getInteger("id"));
            }
        }
        return rst;
    }

    private String storyPoint;

    @Override
    public List<JiraSprintModel> getSprintByProject(String project) {
        List<JiraSprintModel> rst = new ArrayList<>();
        Set<Integer> sprintIds = new HashSet<>();
        List<Integer> boardIds = getBoardIdByProject(project);
        for (Integer boardId : boardIds) {
            JiraHttpResponse response = getRequest(String.format(jiraProperties.getApiSprint(), boardId));
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONArray array = JSONObject.parseObject(body).getJSONArray("values");
                List<JiraSprintModel> sprints = array.toJavaList(JiraSprintModel.class);
                for (JiraSprintModel sprint : sprints) {
                    if (sprintIds.add(sprint.getId())) {
                        rst.add(sprint);
                    }
                }
            }
        }
        return rst;
    }

    @Override
    public List<JiraSubtaskModel> getSubtaskByProject(String project) {
        return null;
    }

    @Override
    public List<JiraSubtaskModel> getSubtaskBySprint(int sprintId) {
        List<JiraSubtaskModel> rst = new ArrayList<>();
        String pointName = jiraProperties.getPointName();
        String jql = jiraProperties.getJqlQuery();
        String jqlQuery = String.format(jql, sprintId, pointName);
        jqlQuery = UriEncoder.encode(jqlQuery);
        JiraHttpResponse response = getRequest(String.format(jiraProperties.getJql(), jqlQuery));
        if (response.isSuccess()) {
            String body = response.getBody();
            JSONObject json = JSONObject.parseObject(body);
            if (storyPoint == null) {
                JSONObject names = json.getJSONObject("names");
                for (Map.Entry<String, Object> entry : names.entrySet()) {
                    if (pointName.equals(entry.getValue())) {
                        storyPoint = entry.getKey();
                        break;
                    }
                }
            }

            JSONArray values = json.getJSONArray("issues");
            for (int i = 0; i < values.size(); i++) {
                JSONObject value = values.getJSONObject(i);
                JiraSubtaskModel task = new JiraSubtaskModel();
                task.setId(value.getInteger("id"));
                task.setKey(value.getString("key"));
                JSONObject fields = value.getJSONObject("fields");
                task.setSummary(fields.getString("summary"));
                task.setProject(fields.getJSONObject("project").getString("name"));
                task.setStoryKey(fields.getJSONObject("parent").getString("key"));
                task.setSprintId(sprintId);
                task.setState(fields.getJSONObject("status").getString("name"));
                task.setPoint(fields.getInteger(storyPoint));
                JSONObject assignee = fields.getJSONObject("assignee");
                task.setAssigneeKey(assignee.getString("key"));
                task.setAssigneeName(assignee.getString("displayName"));
                task.setAvatarUrls(assignee.getJSONObject("avatarUrls").getString("48x48"));
                rst.add(task);
            }
        }
        return rst;
    }
}
