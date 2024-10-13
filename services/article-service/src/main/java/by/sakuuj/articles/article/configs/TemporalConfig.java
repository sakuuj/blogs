package by.sakuuj.articles.article.configs;

import by.sakuuj.articles.article.orchestration.activities.CreateArticleActivities;
import by.sakuuj.articles.article.orchestration.activities.DeleteArticleActivities;
import by.sakuuj.articles.article.orchestration.activities.UpdateArticleActivities;
import by.sakuuj.articles.article.orchestration.workflows.CreateArticleWorkflow;
import by.sakuuj.articles.article.orchestration.workflows.CreateArticleWorkflowImpl;
import by.sakuuj.articles.article.orchestration.workflows.DeleteArticleWorkflow;
import by.sakuuj.articles.article.orchestration.workflows.DeleteArticleWorkflowImpl;
import by.sakuuj.articles.article.orchestration.workflows.UpdateArticleWorkflow;
import by.sakuuj.articles.article.orchestration.workflows.UpdateArticleWorkflowImpl;
import by.sakuuj.articles.article.utils.CompileTimeConstants;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TemporalConfig {

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {

        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget("localhost:7233")
                        .build()
        );
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {

        return WorkflowClient.newInstance(workflowServiceStubs);
    }

    @Bean
    public CreateArticleWorkflow createArticleWorkflowStub(WorkflowClient workflowClient) {

        return workflowClient.newWorkflowStub(
                CreateArticleWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(CompileTimeConstants.TEMPORAL_ARTICLES_QUEUE_NAME)
                        .build()
        );
    }

    @Bean
    public UpdateArticleWorkflow updateArticleWorkflowStub(WorkflowClient workflowClient) {

        return workflowClient.newWorkflowStub(
                UpdateArticleWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(CompileTimeConstants.TEMPORAL_ARTICLES_QUEUE_NAME)
                        .build()
        );
    }

    @Bean
    public DeleteArticleWorkflow deleteArticleWorkflowStub(WorkflowClient workflowClient) {

        return workflowClient.newWorkflowStub(
                DeleteArticleWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(CompileTimeConstants.TEMPORAL_ARTICLES_QUEUE_NAME)
                        .build()
        );
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient,
                                       CreateArticleActivities createArticleActivities,
                                       UpdateArticleActivities updateArticleActivities,
                                       DeleteArticleActivities deleteArticleActivities
                                       ) {

        WorkerFactory workerFactory = WorkerFactory.newInstance(workflowClient);

        Worker worker = workerFactory.newWorker(CompileTimeConstants.TEMPORAL_ARTICLES_QUEUE_NAME);

        worker.registerWorkflowImplementationTypes(
                CreateArticleWorkflowImpl.class,
                UpdateArticleWorkflowImpl.class,
                DeleteArticleWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
                createArticleActivities,
                updateArticleActivities,
                deleteArticleActivities
        );

        return workerFactory;
    }
}