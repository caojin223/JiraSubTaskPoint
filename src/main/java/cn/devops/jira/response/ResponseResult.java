package cn.devops.jira.response;

/**
 * @author cao.jin
 */
public class ResponseResult<T> {

    private Integer status;

    private String msg;

    private Long startTime;

    private Long costTime;

    private T data;

    public ResponseResult() {
    }

    public ResponseResult(Integer status, String msg, Long startTime, Long costTime, T data) {
        this.status = status;
        this.msg = msg;
        this.startTime = startTime;
        this.costTime = costTime;
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", startTime=" + startTime +
                ", costTime=" + costTime +
                ", data=" + data +
                '}';
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
