package likelion.mlb.backendProject.domain.player.controller;


import likelion.mlb.backendProject.domain.player.dto.ElementTypeDto;
import likelion.mlb.backendProject.domain.player.dto.PreviousBestPlayerDto;
import likelion.mlb.backendProject.domain.player.service.ElementTypeService;
import likelion.mlb.backendProject.domain.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/elementType")
public class ElementTypeController {

    private final ElementTypeService elementTypeService;

    @GetMapping("/all")
    public ResponseEntity<List<ElementTypeDto>> getAllElementType() {
        List<ElementTypeDto> elementTypeDtos = elementTypeService.getAllElementType();
        return new ResponseEntity<>(elementTypeDtos, HttpStatus.OK);
    }
}
