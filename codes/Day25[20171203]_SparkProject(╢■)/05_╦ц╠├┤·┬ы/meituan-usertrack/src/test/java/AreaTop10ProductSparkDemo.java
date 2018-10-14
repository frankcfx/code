import com.ibeifeng.senior.usertrack.spark.product.AreaTop10ProductSpark;

/**
 * Created by ibf on 12/03.
 */
public class AreaTop10ProductSparkDemo {
    public static void main(String[] args) throws InterruptedException {
        String[] params = new String[]{"2"};
        AreaTop10ProductSpark.main(params);

        // 为了查看4040页面
        Thread.sleep(1000000);
    }
}
