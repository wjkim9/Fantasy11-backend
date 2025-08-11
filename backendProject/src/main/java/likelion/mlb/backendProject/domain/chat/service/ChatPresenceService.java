package likelion.mlb.backendProject.domain.chat.service;

import java.util.UUID;

public interface ChatPresenceService {
  ChatPresenceResponse join(UUID roomId, UUID userId);
  ChatPresenceResponse leave(UUID roomId, UUID userId);

  class ChatPresenceResponse {
    public final UUID roomId;
    public final UUID userId;
    public final boolean joined;
    public final long onlineCount;

    public ChatPresenceResponse(UUID roomId, UUID userId, boolean joined, long onlineCount) {
      this.roomId = roomId;
      this.userId = userId;
      this.joined = joined;
      this.onlineCount = onlineCount;
    }
  }

}
