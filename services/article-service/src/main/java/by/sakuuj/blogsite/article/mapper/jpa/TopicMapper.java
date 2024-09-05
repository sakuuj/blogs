package by.sakuuj.blogsite.article.mapper.jpa;

import by.sakuuj.blogsite.article.dtos.TopicRequest;
import by.sakuuj.blogsite.article.dtos.TopicResponse;
import by.sakuuj.blogsite.entity.jpa.entities.ArticleTopicEntity;
import by.sakuuj.blogsite.entity.jpa.entities.TopicEntity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TopicMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "modificationAudit", ignore = true)
    TopicEntity toEntity(TopicRequest request);

    @Mapping(target = "createdAt", source = "entity.modificationAudit.createdAt")
    @Mapping(target = "updatedAt", source = "entity.modificationAudit.updatedAt")
    TopicResponse toResponse(TopicEntity entity);

    @Mapping(target = ".", source = "articleTopic.topic")
    TopicEntity toEntity(ArticleTopicEntity articleTopic);

    @InheritConfiguration
    void updateEntity(@MappingTarget TopicEntity entity, TopicRequest request);
}
