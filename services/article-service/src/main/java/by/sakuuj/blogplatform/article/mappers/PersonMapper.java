package by.sakuuj.blogplatform.article.mappers;

import by.sakuuj.blogplatform.article.dtos.PersonRequest;
import by.sakuuj.blogplatform.article.dtos.PersonResponse;
import by.sakuuj.blogplatform.article.entities.jpa.PersonEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "modificationAudit", ignore = true),
    })
    PersonEntity toEntity(PersonRequest request);

    @Mappings({
            @Mapping(target = "createdAt", source = "entity.modificationAudit.createdAt"),
            @Mapping(target = "updatedAt", source = "entity.modificationAudit.updatedAt")
    })
    PersonResponse toResponse(PersonEntity entity);
}
