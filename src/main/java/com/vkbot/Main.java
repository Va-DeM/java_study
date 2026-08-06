package com.vkbot;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vkbot.business.api.VacancyAggregatorApi;
import com.vkbot.business.scheduler.TaskScheduler;
import com.vkbot.business.scheduler.VacancySearchJobFactory;
import com.vkbot.business.service.NotificationService;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.business.service.UserService;
import com.vkbot.business.service.VKApiService;
import com.vkbot.business.service.VacancyService;
import com.vkbot.business.service.impl.NotificationServiceImpl;
import com.vkbot.business.service.impl.SearchTaskServiceImpl;
import com.vkbot.business.service.impl.UserServiceImpl;
import com.vkbot.business.service.impl.VKApiServiceImpl;
import com.vkbot.business.service.impl.VacancyServiceImpl;
import com.vkbot.config.BotConfig;
import com.vkbot.data.repository.SearchTaskRepository;
import com.vkbot.data.repository.UserRepository;
import com.vkbot.data.repository.VacancyRepository;
import com.vkbot.data.repository.impl.FileSearchTaskRepository;
import com.vkbot.data.repository.impl.FileUserRepository;
import com.vkbot.data.repository.impl.FileVacancyRepository;
import com.vkbot.presentation.command.CommandDispatcher;
import com.vkbot.presentation.command.impl.CurrentTasksCommand;
import com.vkbot.presentation.command.impl.DeleteTaskCommand;
import com.vkbot.presentation.command.impl.DoneCommand;
import com.vkbot.presentation.command.impl.ExperienceCommand;
import com.vkbot.presentation.command.impl.KeywordCaptureCommand;
import com.vkbot.presentation.command.impl.KeywordCommand;
import com.vkbot.presentation.command.impl.MainMenuCommand;
import com.vkbot.presentation.command.impl.NewTaskCommand;
import com.vkbot.presentation.command.impl.NextCommand;
import com.vkbot.presentation.command.impl.RegionCommand;
import com.vkbot.presentation.command.impl.SalaryCommand;
import com.vkbot.presentation.command.impl.StartCommand;
import com.vkbot.presentation.command.impl.TaskSelectionCommand;
import com.vkbot.presentation.command.impl.UnknownCommand;
import com.vkbot.presentation.command.impl.UpdateTaskCommand;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.presentation.longpoll.LongPollServer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class Main {
    private static volatile boolean running = true;
    private static final int EXECUTOR_THREADS = 10;

    public static void main(String[] args) {
        log.info("Starting VK Job Bot Application...");

        try {
            // 1. Загрузка конфигурации
            BotConfig config = new BotConfig();
            log.info("Configuration loaded: {}", config);

            // 2. Инициализация VK API
            VkApiClient vkApiClient = new VkApiClient(new HttpTransportClient());
            GroupActor groupActor = new GroupActor((long) Math.toIntExact(config.getGroupId()), config.getVkAccessToken());
            log.info("VK API Client initialized");

            // 3. Инициализация репозиториев
            UserRepository userRepository = new FileUserRepository();
            SearchTaskRepository searchTaskRepository = new FileSearchTaskRepository();
            VacancyRepository vacancyRepository = new FileVacancyRepository();
            log.info("Repositories initialized");

            // 4. Инициализация сервисов
            UserService userService = new UserServiceImpl(userRepository);
            SearchTaskService searchTaskService = new SearchTaskServiceImpl(searchTaskRepository);
            VacancyService vacancyService = new VacancyServiceImpl(vacancyRepository);
            VKApiService vkApiService = new VKApiServiceImpl(vkApiClient, groupActor);
            NotificationService notificationService = new NotificationServiceImpl(vkApiService);
            log.info("Services initialized");

            // 5. Инициализация планировщика задач
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(EXECUTOR_THREADS);
            TaskScheduler taskScheduler = new TaskScheduler(executorService);
            log.info("Task Scheduler initialized with {} threads", EXECUTOR_THREADS);

            // 6. Инициализация фабрики задач поиска вакансий
            VacancyAggregatorApi vacancyApi = new VacancyAggregatorApi();
            VacancySearchJobFactory jobFactory = new VacancySearchJobFactory(vacancyApi, vacancyService, notificationService);

            // 7. Инициализация CommandDispatcher и регистрация всех команд
            CommandDispatcher dispatcher = new CommandDispatcher();
            registerCommands(dispatcher, userService, searchTaskService, vkApiService, taskScheduler, jobFactory);
            log.info("Command Dispatcher initialized with {} commands", dispatcher.getCommands().size());

            // 8. Инициализация LongPoll сервера
            LongPollServer longPollServer = new LongPollServer(vkApiClient, groupActor, config.getGroupId());
            longPollServer.init();
            log.info("LongPoll Server initialized");

            // 9. Восстановление и перезапуск активных задач
            recoverScheduledTasks(searchTaskService, taskScheduler, jobFactory);

            // 10. Регистрация обработчика завершения
            registerShutdownHook(taskScheduler);

            // 11. Основной цикл обработки сообщений
            mainLoop(longPollServer, dispatcher);

        } catch (Exception e) {
            log.error("Fatal error starting bot", e);
            System.exit(1);
        }
    }

    private static void registerCommands(CommandDispatcher dispatcher, UserService userService,
                                         SearchTaskService searchTaskService, VKApiService vkApiService,
                                         TaskScheduler taskScheduler, VacancySearchJobFactory jobFactory) {
        dispatcher.registerAll(
            new StartCommand(userService, vkApiService),
            new MainMenuCommand(vkApiService, searchTaskService, userService),
            new NewTaskCommand(vkApiService, searchTaskService, userService),
            new CurrentTasksCommand(vkApiService, searchTaskService, userService),
            new UpdateTaskCommand(vkApiService, searchTaskService, userService),
            new DeleteTaskCommand(vkApiService, searchTaskService, userService, taskScheduler),
            new RegionCommand(vkApiService),
            new ExperienceCommand(vkApiService),
            new SalaryCommand(vkApiService),
            new KeywordCommand(vkApiService),
            new DoneCommand(vkApiService, searchTaskService, taskScheduler, jobFactory),
            new NextCommand(vkApiService),
            new TaskSelectionCommand(vkApiService, searchTaskService, taskScheduler),
            new KeywordCaptureCommand(vkApiService),
            new UnknownCommand(vkApiService)
        );
    }

    private static void recoverScheduledTasks(SearchTaskService searchTaskService, TaskScheduler taskScheduler,
                                              VacancySearchJobFactory jobFactory) {
        log.info("Recovering scheduled tasks...");
        var activeTasks = searchTaskService.findAllActive();

        if (activeTasks.isEmpty()) {
            log.info("No active tasks to recover");
            return;
        }

        log.info("Found {} active tasks to recover", activeTasks.size());

        for (var task : activeTasks) {
            try {
                taskScheduler.scheduleTask(task.getId(), jobFactory.createJob(task));
                log.info("Task {} scheduled for user {}", task.getId(), task.getUserId());
            } catch (Exception e) {
                log.error("Error recovering task {}", task.getId(), e);
            }
        }
    }

    private static void mainLoop(LongPollServer longPollServer, CommandDispatcher dispatcher) {
        log.info("Starting main processing loop...");

        int reconnectAttempts = 0;
        final int MAX_RECONNECT_ATTEMPTS = 5;

        while (running) {
            try {
                var messages = longPollServer.poll();

                for (MessageDTO message : messages) {
                    try {
                        log.info("Processing message from user {}: {}", message.getUserId(), message.getText());
                        dispatcher.dispatch(message);
                    } catch (Exception e) {
                        log.error("Error processing message from user {}", message.getUserId(), e);
                    }
                }

                reconnectAttempts = 0;

            } catch (Exception e) {
                log.error("Error in main loop, reconnecting...", e);
                reconnectAttempts++;

                if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    log.error("Max reconnect attempts reached ({}), shutting down", MAX_RECONNECT_ATTEMPTS);
                    running = false;
                    break;
                }

                try {
                    Thread.sleep(5000 * reconnectAttempts);
                    longPollServer.reconnect();
                    log.info("Reconnected to LongPoll server (attempt {}/{})", reconnectAttempts, MAX_RECONNECT_ATTEMPTS);
                } catch (Exception reconnectError) {
                    log.error("Failed to reconnect", reconnectError);
                }
            }
        }

        log.info("Main loop terminated");
    }

    private static void registerShutdownHook(TaskScheduler taskScheduler) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received, gracefully stopping...");
            running = false;
            taskScheduler.shutdown();
            log.info("Bot stopped successfully");
        }));
    }
}

