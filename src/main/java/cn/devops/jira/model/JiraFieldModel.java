package cn.devops.jira.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraFieldModel {
    String id;
    String name;
    boolean custom;
    boolean orderable;
    boolean navigable;
    boolean searchable;
    Schema schema;

    @Getter
    @Setter
    public static class Schema {
        String type;
        String items;
        int customId;
    }
}
