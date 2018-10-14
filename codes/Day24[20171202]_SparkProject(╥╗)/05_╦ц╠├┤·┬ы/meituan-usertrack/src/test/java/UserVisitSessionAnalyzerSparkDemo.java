import com.ibeifeng.senior.usertrack.spark.session.UserVisitSessionAnalyzerSpark;

/**
 * Created by ibf on 12/02.
 */
public class UserVisitSessionAnalyzerSparkDemo {
    public static void main(String[] args) throws InterruptedException {
        String[] params = new String[]{"1"};
        UserVisitSessionAnalyzerSpark.main(params);

        // 为了看4040页面，休息一段时间
        Thread.sleep(1000000);
    }
}
