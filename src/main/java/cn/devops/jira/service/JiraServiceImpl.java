package cn.devops.jira.service;

import cn.devops.jira.config.JiraProperties;
import cn.devops.jira.model.*;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author caojin
 */
@Slf4j
@Service
public class JiraServiceImpl implements JiraService {


    CloseableHttpClient client = HttpClients.createDefault();

    private final JiraProperties jiraProperties;

    public JiraServiceImpl(JiraProperties jiraProperties) {
        this.jiraProperties = jiraProperties;
    }

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
        log.info("GET url: " + getRequest.getURI());
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

    private JiraHttpResponse postJqlRequest(List<Integer> sprintIds) {
        JiraHttpResponse rst = new JiraHttpResponse();
        HttpPost httpPost = new HttpPost(jiraProperties.getHost() + jiraProperties.getJql());
        log.info("POST url: " + httpPost.getURI());
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
        String auth = Base64Util.encode(jiraProperties.getUsername() + ":" + jiraProperties.getPassword());
        httpPost.setHeader("Authorization", "Basic " + auth);

        JSONObject json = new JSONObject();
        json.put("maxResults", -1);
        JSONArray fields = json.putArray("fields");
        fields.addAll(Arrays.asList("summary", "assignee", "status", taskPointKey, sprintKey));
        String ids = sprintIds.stream().map(Object::toString).collect(Collectors.joining(","));
        json.put("jql", String.format(jiraProperties.getJqlQuery(), ids));
        StringEntity bodyEntity = new StringEntity(json.toString(), "utf-8");
        bodyEntity.setContentEncoding("UTF-8");
        bodyEntity.setContentType("application/json");
        httpPost.setEntity(bodyEntity);

        try (CloseableHttpResponse response = client.execute(httpPost)) {
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

    @Override
    public List<JiraSprintModel> getSprintByProjectKey(String projectKey) {
        List<JiraSprintModel> rst = new ArrayList<>();
        Set<Integer> sprintIds = new HashSet<>();
        List<Integer> boardIds = getBoardIdByProject(projectKey);
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
        rst.sort(Comparator.comparingInt(JiraSprintModel::getId));
        return rst;
    }

    private int getSprintId(JSONObject issue) {
        JSONObject fields = issue.getJSONObject("fields");
        String sprintStr = fields.getJSONArray(sprintKey).getString(0);
        sprintStr = sprintStr.substring(sprintStr.indexOf("[") + 1, sprintStr.length() - 1);
        String[] split = sprintStr.split(",");
        for (String item : split) {
            String[] items = item.split("=");
            if ("id".equals(items[0])) {
                return Integer.parseInt(items[1]);
            }
        }
        return -1;
    }

    private void addTaskToAssignee(JSONObject issue, Map<Integer, Map<String, JiraAssigneeResModel>> sprintMap) {
        JiraSubtaskModel task = new JiraSubtaskModel();
        task.setId(issue.getString("id"));
        task.setKey(issue.getString("key"));
        JSONObject fields = issue.getJSONObject("fields");
        task.setSummary(fields.getString("summary"));
        task.setStatus(fields.getJSONObject("status").getString("name"));
        task.setPoint(fields.getInteger(taskPointKey));
        JSONObject assignee = fields.getJSONObject("assignee");
        String assigneeName = assignee == null ? null : assignee.getString("key");
        int sprintId = getSprintId(issue);
        Map<String, JiraAssigneeResModel> map = sprintMap.computeIfAbsent(sprintId, v -> new HashMap<>());
        JiraAssigneeResModel assigneeRes = map.computeIfAbsent(assigneeName, v -> {
            JiraAssigneeResModel e;
            if (assignee != null) {
                e = assignee.to(JiraAssigneeResModel.class);
                e.setAvatarUrl(assignee.getJSONObject("avatarUrls").getString("48x48"));
            } else {
                e = new JiraAssigneeResModel();
            }
            return e;
        });
        assigneeRes.setPointCount(assigneeRes.getPointCount() + task.getPoint());
        assigneeRes.getSubtasks().add(task);
    }

    @Override
    public List<JiraSprintResModel> getSubtaskByProjectKey(String projectKey) {
        List<JiraSprintResModel> rst = getSprintByProjectKey(projectKey).stream()
                .map(JiraSprintResModel::init).collect(Collectors.toList());
        if (rst.size() > 0) {
            List<Integer> sprintIds = new ArrayList<>();
            rst.forEach(e -> sprintIds.add(e.getId()));
            Map<Integer, Map<String, JiraAssigneeResModel>> sprint2assigneeMap = new HashMap<>(rst.size());
            JiraHttpResponse response = postJqlRequest(sprintIds);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject json = JSONObject.parseObject(body);
                JSONArray issues = json.getJSONArray("issues");
                for (int i = 0; i < issues.size(); i++) {
                    JSONObject issue = issues.getJSONObject(i);
                    addTaskToAssignee(issue, sprint2assigneeMap);
                }
            }
            for (JiraSprintResModel sprint : rst) {
                Map<String, JiraAssigneeResModel> assigneeMap = sprint2assigneeMap.get(sprint.getId());
                JiraAssigneeResModel nullAssignee = null;
                if (assigneeMap.containsKey(null)) {
                    nullAssignee = assigneeMap.remove(null);
                }
                List<JiraAssigneeResModel> assignees = assigneeMap.values().stream()
                        .sorted((o1, o2) -> Integer.compare(o2.getPointCount(), o1.getPointCount()))
                        .collect(Collectors.toList());
                if (nullAssignee != null) {
                    sprint.getAssignees().add(nullAssignee);
                }
                sprint.getAssignees().addAll(assignees);
            }
        }

        return rst;
    }

    private String taskPointKey;
    private String sprintKey;

    @PostConstruct
    void initCustomField() {
        List<JiraFieldModel> fields = getFields();
        for (JiraFieldModel field : fields) {
            if (field.isCustom()) {
                if (jiraProperties.getPointName().equals(field.getName())) {
                    taskPointKey = field.getId();
                }
                if (jiraProperties.getSprintName().equals(field.getName())) {
                    sprintKey = field.getId();
                }
                if (taskPointKey != null && sprintKey != null) {
                    break;
                }
            }
        }
    }

    private List<JiraFieldModel> getFields() {
        List<JiraFieldModel> rst = new ArrayList<>();
        JiraHttpResponse response = getRequest(jiraProperties.getApiField());
        if (response.isSuccess()) {
            String body = response.getBody();
            JSONArray array = JSONArray.parse(body);
            return array.toJavaList(JiraFieldModel.class);
        }
        return rst;
    }
}