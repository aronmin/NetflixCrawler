import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.NoSuchElementException;

import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
        Thread.sleep(3000);

        List<WebElement> elements = driver.findElements(By.cssSelector(".nm-collections-title.nm-collections-link"));
        for (WebElement element : elements) {
            String url = element.getAttribute("href");
            if (url != null && !links.contains(url)) {
                String img_url = element.findElement(By.tagName("img")).getAttribute("src");
                links.add(url);
                imgs.add(img_url);
            }
        }
/**
        System.out.println(links.size());
        System.out.println(imgs.size());
        List<WebElement> img_elements = driver.findElements(By.cssSelector(".nm-collections-title-img"));
        for (WebElement img_element : img_elements) {
            if(img_element.getAttribute("class").equals("nm-collections-title-img")) {
                String img_url = img_element.getAttribute("src");
                if (!links.contains(img_url)) {
                    imgs.add(img_url);
                }
            }
        }
*/
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("NetflixMovies.txt"))) {
            try {
                for (String link : links) {
                    driver.get(link);
                    Thread.sleep(3000);

                    WebElement TitleElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/h1"));
                    String title = TitleElement.getText();
                    titles.add(title);
                    System.out.println((cnt+1) + "번째 영화");
                    System.out.println("title : " + title);
                    writer.write((cnt+1) + "번재 영화\n");
                    writer.write("Title : " + title + "\n");

                    try {
                        WebElement DirElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[2]/div[2]/div[2]/span[2]"));
                        String director = DirElement.getText();
                        directors.add(director);
                        System.out.println("director : " + director);
                        writer.write("director : " + director + "\n");
                    } catch(NoSuchElementException e) {
                        directors.add("정보없음");
                        System.out.println("director : 정보없음");
                        writer.write("director : 정보없음\n");
                    }

                    try {
                        WebElement ActorElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[2]/div[2]/div[1]/span[2]"));
                        String actor = ActorElement.getText();
                        actors.add(actor);
                        System.out.println("actor : " + actor);
                        writer.write("actor : " + actor + "\n");
                    } catch(NoSuchElementException e) {
                        actors.add("정보없음");
                        System.out.println("actor : 정보없음");
                        writer.write("actor : 정보없음\n");
                    }

                    WebElement DescriptionElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[2]/div[1]"));
                    String description = DescriptionElement.getText();
                    descriptions.add(description);
                    System.out.println("description : " + description);
                    writer.write("description : " + description + "\n");

                    WebElement GenreElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[1]/a"));
                    String genre = GenreElement.getText();
                    genres.add(genre);
                    System.out.println("genre : " + genre);
                    writer.write("genre : " + genre + "\n");
                    System.out.println("link : " + links.get(cnt));
                    writer.write("link : " + links.get(cnt) + "\n");
                    System.out.println("img : " + imgs.get(cnt));
                    writer.write("img : " + imgs.get(cnt) + "\n");
                    System.out.println("-------------------------------------------------------");
                    writer.write("-------------------------------------------------------\n");
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
