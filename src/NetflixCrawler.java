import org.openqa.selenium.*;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class NetflixCrawler {
    private final WebDriver driver;
    private final List<String> links;
    private final List<String> titles;
    private final List<String> genres;
    private final List<String> directors;
    private final List<String> actors;
    private final List<String> descriptions;
    private final List<String> imgs;

    public NetflixCrawler(WebDriver driver) {
        this.driver = driver;
        this.links = new ArrayList<>();
        titles = new ArrayList<>();
        genres = new ArrayList<>();
        directors = new ArrayList<>();
        actors = new ArrayList<>();
        descriptions = new ArrayList<>();
        imgs = new ArrayList<>();
    }
    int cnt = 0;

    public void crawl(String startUrl) throws InterruptedException, IOException {
        driver.get(startUrl);

        List<WebElement> elements = driver.findElements(By.cssSelector(".nm-collections-title.nm-collections-link"));
        List<WebElement> img_elements = driver.findElements(By.cssSelector(".nm-collections-title-img"));
        for (WebElement element : elements) {
            String url = element.getAttribute("href");
            links.add(url);
        }
        for (WebElement img_element : img_elements) {
            if("nm-collections-title-img".equals(img_element.getAttribute("class"))) {
                String img_url = img_element.getAttribute("src");
                imgs.add(img_url);
            }
        }
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("NetflixMovies.txt"))) {
            try {
                for (String link : links) {
                    driver.get(link);
                    Thread.sleep(2000);

                    WebElement TitleElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/h1"));
                    String title = TitleElement.getText();
                    titles.add(title);
                    System.out.println((cnt+1) + "번째 영화");
                    System.out.println("title : " + title);

                    try {
                        WebElement DirElements = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[2]/div[2]/div[2]/span[2]"));
                        String director = DirElements.getText();
                            directors.add(director);
                            System.out.println("director : " + director);
                    } catch(NoSuchElementException e) {
                        directors.add("정보없음");
                        System.out.println("정보없음");
                    }


                    List<WebElement> ActorElements = driver.findElements(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[2]/div[2]/div[1]/span[2]"));
                    String Actor = ActorElements.getText();

                    WebElement descriptionElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[2]/div[1]"));

                    System.out.println("-------------------------------------------------------");
                    cnt++;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }


}
