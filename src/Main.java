import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver; 

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException{
        System.setProperty("webdriver.chrome.driver", "util/driver/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        NetflixCrawler crawler = new NetflixCrawler(driver);

        String startUrl = "https://www.netflix.com/kr/browse/genre/839338";

        try {
            crawler.crawl(startUrl);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}