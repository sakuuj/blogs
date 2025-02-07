package by.sakuuj.articles.repository.elasticsearch.custom;

import by.sakuuj.annotations.ElasticsearchTest;
import by.sakuuj.articles.article.ArticleDocumentTestBuilder;
import by.sakuuj.articles.article.entity.elasticsearch.ArticleDocument;
import by.sakuuj.articles.article.repository.elasticsearch.ArticleDocumentRepository;
import by.sakuuj.articles.article.repository.elasticsearch.custom.SearchHitsToPageViewMapper;
import by.sakuuj.articles.article.repository.elasticsearch.custom.SearchHitsToPageViewMapperImpl;
import by.sakuuj.articles.paging.PageView;
import by.sakuuj.testcontainers.ElasticsearchSingletonContainerLauncher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ElasticsearchTest
class SearchHitsToPageViewMapperImplIntegrationTests extends ElasticsearchSingletonContainerLauncher {

    @Autowired
    private SearchHitsToPageViewMapper searchHitsToPageViewMapper;

    @Autowired
    private ArticleDocumentRepository articleDocumentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @AfterEach
    void removeAllDocumentsFromIndex() {
        articleDocumentRepository.deleteAll(RefreshPolicy.IMMEDIATE);
        Iterable<ArticleDocument> allDocuments = articleDocumentRepository.findAll();
        assertThat(allDocuments.iterator().hasNext()).isFalse();
    }

    @Test
    void shouldAutowireSearchHitsToPageViewMapperImpl() {

        assertThat(AopUtils.getTargetClass(searchHitsToPageViewMapper))
                .isSameAs(SearchHitsToPageViewMapperImpl.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldMapHitsToPageView(boolean firstDocComesLast) {
        ArticleDocumentTestBuilder testDataBuilder = ArticleDocumentTestBuilder.anArticleDocument();

        ArticleDocument firstDoc = testDataBuilder
                .withTitle("TITLE_1 ")
                .withContent("CONTENT_1 ")
                .withId(UUID.fromString("920e3446-ce92-4216-85d5-c7b9e76423d4"))
                .buildDocument();

        ArticleDocument secondDoc = testDataBuilder
                .withTitle("TITLE_2 ")
                .withContent("CONTENT_2 ")
                .withId(UUID.fromString("6d55e28d-16c0-4a68-b10e-fbccd24f6908"))
                .buildDocument();

        List<ArticleDocument> docsToSave = new ArrayList<>(List.of(firstDoc, secondDoc));
        List<UUID> expectedContent = new ArrayList<>(List.of(firstDoc.getId(), secondDoc.getId()));
        if (firstDocComesLast) {
            Collections.reverse(docsToSave);
            Collections.reverse(expectedContent);
        }

        articleDocumentRepository.saveAll(docsToSave, RefreshPolicy.IMMEDIATE);

        SearchHits<ArticleDocument> searchHitsToMap = elasticsearchOperations.search(Query.findAll(), ArticleDocument.class);

        int expectedPageSize = 33;
        int expectedPageNumber = 3;

        // when
        PageView<UUID> mapped = searchHitsToPageViewMapper.map(searchHitsToMap, PageRequest.of(expectedPageNumber, expectedPageSize), ArticleDocument::getId);

        Assertions.assertThat(mapped.content()).isEqualTo(expectedContent);
    }
}
