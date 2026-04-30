import org.jsoup.Jsoup;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class test_feed {
    public static void main(String[] args) {
        try {
            String FEED_URL = "https://ipl-stats-sports-mechanic.s3.ap-south-1.amazonaws.com/ipl/feeds/284-matchschedule.js";
            String jsonp = Jsoup.connect(FEED_URL)
                    .ignoreContentType(true)
                    .timeout(60000)
                    .maxBodySize(0)
                    .execute()
                    .body();
            
            System.out.println("Fetched length: " + jsonp.length());
            
            int start = jsonp.indexOf('{');
            int end = jsonp.lastIndexOf('}');
            System.out.println("JSON start: " + start + " end: " + end);
            
            if (start != -1 && end != -1) {
                String json = jsonp.substring(start, end + 1);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonArray matchSummaries = root.getAsJsonArray("Matchsummary");
                System.out.println("Matches found: " + matchSummaries.size());
                
                if (matchSummaries.size() > 0) {
                    JsonObject first = matchSummaries.get(0).getAsJsonObject();
                    System.out.println("First match teams: " + 
                        first.get("FirstBattingTeamName").getAsString() + " vs " +
                        first.get("SecondBattingTeamName").getAsString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
