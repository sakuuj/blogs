package by.sakuuj.blogplatform.article.repositories.elasticsearch;

import by.sakuuj.blogplatform.article.entities.ArticleDocument;
import by.sakuuj.blogplatform.article.repositories.PageView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ResourceUtil;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticleCustomElasticsearchRepositoryImpl implements ArticleCustomElasticsearchRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final String FIND_MOST_RELEVANT_FOR_SEARCH_TERMS_QUERY = ResourceUtil
            .readFileFromClasspath("elasticsearchQueries/findMostRelevantForSearchTermsQuery.json");


    @SuppressWarnings("unchecked")
    public PageView<ArticleDocument> findMostRelevantDocuments(String searchTerms, Pageable pageable) {

        String actualQueryContent = FIND_MOST_RELEVANT_FOR_SEARCH_TERMS_QUERY
                .replaceAll("\\?0", searchTerms);

        StringQuery actualQuery =  StringQuery.builder(actualQueryContent)
                .withPageable(pageable)
                .withTrackTotalHits(false)
                .build();

        SearchHits<ArticleDocument> searchHits = elasticsearchOperations.search(actualQuery, ArticleDocument.class);
        SearchPage<ArticleDocument> searchedPage = SearchHitSupport.searchPageFor(searchHits, actualQuery.getPageable());

        List<ArticleDocument> content = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toCollection(ArrayList::new));

        return new PageView<>(
                content,
                searchedPage.getSize(),
                searchedPage.getNumber()
        );
    }
}