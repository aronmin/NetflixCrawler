import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;

import db.DBInsert;
import java.sql.SQLException;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
    private DBInsert dbInsert = new DBInsert();
    int cnt = 0;

    public void crawl(String startUrl) throws InterruptedException, IOException {
        driver.get(startUrl);
        Thread.sleep(2000);

        // 이미지 로드
        By buttonSelector = By.cssSelector(".nm-content-horizontal-row-nav.next");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        while (true) {
            try {
                WebElement button = driver.findElement(buttonSelector);
                while(!button.isDisplayed() || !isElementNearBottom(button)){
                    js.executeScript("window.scrollBy(0,500)");
                    Thread.sleep(2000);
                }
                button.click();
                Thread.sleep(3000);
            } catch (NoSuchElementException e) {
                System.out.println("Button is no longer present.");
                break;
            }
        }

        // 링크, 이미지 url
        List<WebElement> elements = driver.findElements(By.cssSelector(".nm-collections-title.nm-collections-link"));
        String NoImage = "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==";
        boolean ImgError = false;
        for (WebElement element : elements) {
            String url = element.getAttribute("href");
            if (url != null && !links.contains(url)) {
                String img_url = element.findElement(By.tagName("img")).getAttribute("src");
                links.add(url);
                imgs.add(img_url);
                if (Objects.equals(img_url, NoImage))
                    ImgError = true;
                System.out.println(img_url);
            }
        }

        // 이미지 로드가 안된 항목이 있을 경우 프로그램 종료
        System.out.println("컨턴츠 개수 : " + links.size());
        System.out.println("이미지 개수 : " + imgs.size());
        if (ImgError)
            System.exit(1);

        Set<String> uniqueGenres = new HashSet<>();

        // 각 컨텐츠 요소 탐색 및 저장
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("NetflixMovies.txt"));
            BufferedWriter genreWriter = new BufferedWriter(new FileWriter("Genres.txt"))) {
            try {
                for (String link : links) {
                    driver.get(link);
                    Thread.sleep(3000);

                    // 제목
                    WebElement TitleElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/h1"));
                    String title = TitleElement.getText();
                    titles.add(title);
                    System.out.println((cnt+1) + "번째 영화");
                    System.out.println("title : " + title);
                    writer.write((cnt+1) + "번재 영화\n");
                    writer.write("Title : " + title + "\n");

                    // 감독
                    try {
                        WebElement DirElement = driver.findElement(By.cssSelector(".title-data-info-item-list[data-uia='info-creators']"));
                        String director = DirElement.getText();
                        directors.add(director);
                        System.out.println("director : " + director);
                        writer.write("director : " + director + "\n");
                    } catch(NoSuchElementException e) {
                        directors.add("정보없음");
                        System.out.println("director : 정보없음");
                        writer.write("director : 정보없음\n");
                    }

                    // 주연
                    try {
                        List <WebElement> ActorElements = driver.findElements(By.cssSelector(".more-details-item.item-cast"));
                        StringBuilder actorText = new StringBuilder();
                        for (int i = 0; i < ActorElements.size(); i++) {
                            String actor = ActorElements.get(i).getText().trim(); // 배우 이름 양 옆의 공백 제거
                            actorText.append(actor);
                            if (i < ActorElements.size() - 1) {
                                actorText.append(", "); // 마지막 배우 이후에는 쉼표 추가하지 않음
                            }
                        }
                        if(actorText.isEmpty()) {
                            actors.add("정보없음");
                            System.out.println("actor : 정보없음");
                            writer.write("actor : 정보없음\n");
                        } else {
                            actors.add(actorText.toString().trim());
                            System.out.println("actor : " + actorText);
                            writer.write("actor : " + actorText + "\n");
                        }
                    } catch(NoSuchElementException e) {
                        actors.add("정보없음");
                        System.out.println("actor : 정보없음");
                        writer.write("actor : 정보없음\n");
                    }

                    // 설명
                    WebElement DescriptionElement = driver.findElement(By.cssSelector(".title-info-synopsis[data-uia='title-info-synopsis']"));
                    String description = DescriptionElement.getText();
                    descriptions.add(description);
                    System.out.println("description : " + description);
                    writer.write("description : " + description + "\n");

                    // 장르
                    WebElement GenreElement = driver.findElement(By.xpath("//*[@id=\"section-hero\"]/div[1]/div[1]/div[2]/div/div[1]/a"));
                    String genre = GenreElement.getText();
                    genres.add(genre);
                    System.out.println("genre : " + genre);
                    writer.write("genre : " + genre + "\n");

                    // 장르 요소 확인
                    if (!uniqueGenres.contains(genre)) {
                        genreWriter.write("genre : " + genre + "\n");
                        uniqueGenres.add(genre);
                    }

                    System.out.println("link : " + links.get(cnt));
                    writer.write("link : " + links.get(cnt) + "\n");
                    System.out.println("img : " + imgs.get(cnt));
                    writer.write("img : " + imgs.get(cnt) + "\n");

                    // DB삽입
                    try {
                        if (!dbInsert.isDataExists(titles.get(cnt), directors.get(cnt))) {
                            dbInsert.contentInsert(titles.get(cnt), imgs.get(cnt), descriptions.get(cnt), directors.get(cnt), actors.get(cnt), links.get(cnt), "netflix");
                            switch(genres.get(cnt)){
                                case "호러":
                                    dbInsert.genreInsert(dbInsert.searchMovieID("netflix", titles.get(cnt)), 20, "netflix_genre");
                                    break;
                                case "드라마 장르":
                                    dbInsert.genreInsert(dbInsert.searchMovieID("netflix", titles.get(cnt)), 2, "netflix_genre");
                                    break;
                                case "리얼리티 시리즈":
                                    dbInsert.genreInsert(dbInsert.searchMovieID("netflix", titles.get(cnt)), 12, "netflix_genre");
                                    break;
                                case "애니":
                                    dbInsert.genreInsert(dbInsert.searchMovieID("netflix", titles.get(cnt)), 9, "netflix_genre");
                                    break;
                                case "버라이어티 시리즈":
                                    dbInsert.genreInsert(dbInsert.searchMovieID("netflix", titles.get(cnt)), 1, "netflix_genre");
                                    break;
                                default:
                                    Integer genreId = dbInsert.searchGenreID(genres.get(cnt));
                                    dbInsert.genreInsert(dbInsert.searchMovieID("netflix", titles.get(cnt)), genreId, "netflix_genre");
                                    break;
                            }
                        } else {
                            System.out.println("이미 데이터가 존재합니다: " + titles.get(cnt) + ", " + directors.get(cnt));
                        }
                    } catch (SQLException e) {
                        System.err.println("DB 삽입 중 오류가 발생했습니다: " + e.getMessage());
                    } catch (NullPointerException e){
                        System.err.println("장르 삽입 오류 발생 항목 : " + titles.get(cnt));
                        System.err.println("DB 장르 삽입 중 오류가 발생했습니다: " + e.getMessage());
                        break;
                    }
                    cnt++;
                    System.out.println("-------------------------------------------------------");
                    writer.write("-------------------------------------------------------\n");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    // 이미지 로드 중 스크롤 제어
    private boolean isElementNearBottom(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Number elementPosition = (Number) js.executeScript("return arguments[0].getBoundingClientRect().bottom;", element);
        Number windowHeight = (Number) js.executeScript("return window.innerHeight;");
        return elementPosition.doubleValue() <= windowHeight.doubleValue();
    }
}
