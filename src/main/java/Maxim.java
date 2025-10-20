import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class Maxim {
    private String sentence;    // 摘抄的句子
    private String analysis;    // 句子解析
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date saveTime;      // 保存时间

    public Maxim() {
        this.saveTime = new Date();
    }

    public Maxim(String sentence, String analysis) {
        this.sentence = sentence;
        this.analysis = analysis;
        this.saveTime = new Date();
    }

    // Getters and Setters
    public String getSentence() { return sentence; }
    public void setSentence(String sentence) { this.sentence = sentence; }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public Date getSaveTime() { return saveTime; }
    public void setSaveTime(Date saveTime) { this.saveTime = saveTime; }

    @Override
    public String toString() {
        return String.format("句子: %s\n解析: %s\n时间: %s",
                sentence, analysis, saveTime);
    }
}