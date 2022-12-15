package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class JiraSprintResModel extends JiraSprintModel {

    List<JiraAssigneeResModel> assignees = new ArrayList<>();

    public static JiraSprintResModel init(JiraSprintModel sprint) {
        JiraSprintResModel rst = new JiraSprintResModel();
        BeanUtils.copyProperties(sprint, rst);
        return rst;
    }

}

