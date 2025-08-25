package likelion.mlb.backendProject.domain.draft.util;

import java.util.UUID;

public class DraftKeys {
    public static String state(UUID roomId) { return "draft:"+roomId+":state"; }
    public static String timer(UUID roomId) { return "draft:"+roomId+":timer"; }
    public static String topic(UUID roomId) { return "/topic/draft."+roomId; }
}