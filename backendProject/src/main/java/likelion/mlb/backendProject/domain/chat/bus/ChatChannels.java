package likelion.mlb.backendProject.domain.chat.bus;

import java.util.UUID;

public final class ChatChannels {
  private ChatChannels() {}
  public static String roomChannel(UUID roomId) { return "chat:room:" + roomId; }
  public static String toTopic(String channel) { return channel.replace("chat:room:", "/topic/chat/"); }
}