package cn.devops.jira.model;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraSubtaskModel {
    int id;
    String key;
    String summary;
    String type = "subtask";
    String project;
    String storyKey;
    int sprintId;
    String state;
    int point;
    String assigneeKey;
    String assigneeName;
    String avatarUrls;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
