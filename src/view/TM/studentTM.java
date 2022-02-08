package view.TM;

public class studentTM {
    private String stId;
    private int grade;
    private String name;
    private byte[]  img;
    private String contact;

    public studentTM(String stId, int grade, String name, byte[]  img, String contact) {
        this.stId = stId;
        this.grade = grade;
        this.name = name;
        this.img = img;
        this.contact = contact;
    }

    public studentTM() {
    }

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[]  getImg() {
        return img;
    }

    public void setImg(byte[]  img) {
        this.img = img;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "studentTM{" +
                "stId='" + stId + '\'' +
                ", grade=" + grade +
                ", name='" + name + '\'' +
                ", img=" + img +
                ", contact='" + contact + '\'' +
                '}';
    }
}
