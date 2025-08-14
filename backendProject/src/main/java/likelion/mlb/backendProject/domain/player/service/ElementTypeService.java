package likelion.mlb.backendProject.domain.player.service;


import likelion.mlb.backendProject.domain.player.dto.ElementTypeDto;
import likelion.mlb.backendProject.domain.player.entity.ElementType;
import likelion.mlb.backendProject.domain.player.repository.ElementTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementTypeService {

    private final ElementTypeRepository elementTypeRepository;

    public List<ElementTypeDto> getAllElementType(){
        List<ElementType> elementTypes = elementTypeRepository.findAll();

        return ElementType.toDtoList(elementTypes);
    }
}
