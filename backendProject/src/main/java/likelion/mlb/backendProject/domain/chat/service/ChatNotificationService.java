package likelion.mlb.backendProject.domain.chat.service;


import likelion.mlb.backendProject.domain.player.entity.live.MatchEvent;

public interface ChatNotificationService {

  void sendMatchAlert(MatchEvent matchEvent);
}
