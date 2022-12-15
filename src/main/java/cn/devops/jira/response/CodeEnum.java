package cn.devops.jira.response;

/**
 * @author cao.jin
 */

public enum CodeEnum {

    SUCCESS(200,"success"),
    ERROR(-200,"error");

    private Integer code;

    private String msg;

    CodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
