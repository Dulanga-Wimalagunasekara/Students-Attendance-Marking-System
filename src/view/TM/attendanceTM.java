package view.TM;

import java.time.LocalDateTime;

public class attendanceTM {
    private int order;
    private LocalDateTime date;
    private String name;
    private int grade;
    private String status;
    private String studentId;
    private String operator;

    public attendanceTM(int order, LocalDateTime date, String name, int grade, String status, String studentId, String operator) {

        this.order = order;
        this.date = date;
        this.name = name;
        this.grade = grade;
        this.status = status;
        this.studentId = studentId;
        this.operator = operator;
    }

    public attendanceTM() {

    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "attendanceTM{" +
                "order=" + order +
                ", date=" + date +
                ", name='" + name + '\'' +
                ", grade=" + grade +
                ", status='" + status + '\'' +
                ", studentId='" + studentId + '\'' +
                ", operator='" + operator + '\'' +
                '}';
    }
}
