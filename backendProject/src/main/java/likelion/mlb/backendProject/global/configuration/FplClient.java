package likelion.mlb.backendProject.global.configuration;

import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.BootstrapStatic;
import likelion.mlb.backendProject.global.staticdata.dto.fixture.FplFixture;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveEventDto;

import java.util.List;

public interface FplClient {
    BootstrapStatic getBootstrapStatic();
    List<FplFixture> getFixtures(int event);
    LiveEventDto getLive(int round);
}
