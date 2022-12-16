package cn.devops.jira.service;

import cn.devops.jira.config.JiraProperties;
import cn.devops.jira.model.*;
import cn.devops.jira.util.TimeTools;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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
        url = url.replace("//", "/");
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        HttpGet getRequest = new HttpGet(jiraProperties.getHost() + url);
        return httpRequest(getRequest);
    }

    private JiraHttpResponse postJqlRequest(String jql) {
        HttpPost httpPost = new HttpPost(jiraProperties.getHost() + jiraProperties.getJql());
        log.info("POST url: " + httpPost.getURI());
        JSONObject json = new JSONObject();
        json.put("maxResults", -1);
        JSONArray fields = json.putArray("fields");
        fields.addAll(Arrays.asList("summary", "assignee", "status", taskPointKey, sprintKey));
        json.put("jql", jql);
        StringEntity bodyEntity = new StringEntity(json.toString(), "utf-8");
        bodyEntity.setContentEncoding("UTF-8");
        bodyEntity.setContentType("application/json");
        httpPost.setEntity(bodyEntity);
        return httpRequest(httpPost);
    }

    private JiraHttpResponse httpRequest(HttpUriRequest request) {
        JiraHttpResponse rst = new JiraHttpResponse();
        log.info("{}: {}", request.getMethod(), request.getURI());
        request.setHeader("Accept", "application/json");
        request.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
        String auth = Base64Util.encode(jiraProperties.getUsername() + ":" + jiraProperties.getPassword());
        request.setHeader("Authorization", "Basic " + auth);
        long startTime = TimeTools.now();
        try (CloseableHttpResponse response = client.execute(request)) {
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
        log.info("Total time: {}ms", TimeTools.now() - startTime);
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

    private JiraSprintResModel getSprintByIssue(Map<Integer, JiraSprintResModel> sprintMap, JSONObject issue) {
        JSONObject fields = issue.getJSONObject("fields");
        String sprintStr = fields.getJSONArray(sprintKey).getString(0);
        sprintStr = sprintStr.substring(sprintStr.indexOf("[") + 1, sprintStr.length() - 1);
        String[] split = sprintStr.split(",");
        JSONObject json = new JSONObject();
        for (String item : split) {
            String[] items = item.split("=");
            if (items.length == 2) {
                json.put(items[0], "<null>".equals(items[1]) ? null : items[1]);
            }
        }
        Integer sprintId = json.getInteger("id");
        return sprintMap.computeIfAbsent(sprintId, v -> json.to(JiraSprintResModel.class));
    }

    private List<JiraSprintResModel> createSprintsByIssues(JSONArray issues) {
        // sprintId -> sprint
        Map<Integer, JiraSprintResModel> sprintMap = new HashMap<>();
        // sprintId -> assigneeKey -> assignee
        Map<Integer, Map<String, JiraAssigneeResModel>> sprint2AssigneeMap = new HashMap<>();
        for (int i = 0; i < issues.size(); i++) {
            JSONObject issue = issues.getJSONObject(i);

            JiraSubtaskModel task = new JiraSubtaskModel();
            task.setId(issue.getString("id"));
            task.setKey(issue.getString("key"));
            JSONObject fields = issue.getJSONObject("fields");
            task.setSummary(fields.getString("summary"));
            task.setStatus(fields.getJSONObject("status").getString("name"));
            task.setPoint(fields.getInteger(taskPointKey));
            JSONObject assignee = fields.getJSONObject("assignee");
            String assigneeKey = assignee == null ? null : assignee.getString("key");

            JiraSprintResModel sprint = getSprintByIssue(sprintMap, issue);

            // assigneeKey -> assignee
            Map<String, JiraAssigneeResModel> assigneeMap =
                    sprint2AssigneeMap.computeIfAbsent(sprint.getId(), v -> new HashMap<>());
            JiraAssigneeResModel assigneeRes = assigneeMap.computeIfAbsent(assigneeKey, v -> {
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

        List<JiraSprintResModel> sprints = sprintMap.values().stream()
                // 先Active，再Future
                .sorted((o1, o2) -> {
                    if (Objects.equals(o1.getState(), o2.getState())) {
                        return Integer.compare(o1.getId(), o2.getId());
                    } else {
                        return o1.getState().compareTo(o2.getState());
                    }
                })
                .collect(Collectors.toList());
        for (JiraSprintResModel sprint : sprints) {
            Map<String, JiraAssigneeResModel> assigneeMap = sprint2AssigneeMap.get(sprint.getId());
            JiraAssigneeResModel anonymous = assigneeMap.remove(null);
            if (anonymous != null) {
                sprint.getAssignees().add(anonymous);
            }
            List<JiraAssigneeResModel> sortedAssignee = assigneeMap.values()
                    .stream().sorted((o1, o2) -> Integer.compare(o2.getPointCount(), o1.getPointCount()))
                    .collect(Collectors.toList());
            sprint.getAssignees().addAll(sortedAssignee);
        }
        return sprints;
    }

    @Override
    public List<JiraSprintResModel> getSubtaskByProjectKey(String projectKey) {
        List<JiraSprintResModel> rst = new ArrayList<>();
        String jql = String.format(jiraProperties.getJqlByProject(), projectKey, jiraProperties.getPointName());
        JiraHttpResponse response = postJqlRequest(jql);
        if (response.isSuccess()) {
            String body = response.getBody();
            JSONObject json = JSONObject.parseObject(body);
            JSONArray issues = json.getJSONArray("issues");
            rst = createSprintsByIssues(issues);
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
