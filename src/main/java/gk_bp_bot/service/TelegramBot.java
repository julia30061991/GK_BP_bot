package gk_bp_bot.service;

import gk_bp_bot.config.BotConfig;
import gk_bp_bot.model.User;
import gk_bp_bot.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;

    @Autowired
    private UserRepo userRepo;

    private final String ALL_VAKANCIES = "https://hh.ru/search/vacancy?ored_clusters=true&search_field=name&hhtmFrom=vacancy_search_list&text=&enable_snippets=false&employer_id=1966177&L_save_area=true";

    private final String ANSWER_FOR_ALL = "Выберите нужную Вам специализацию из представленного ниже списка ⬇ \n\n ❗ В случае, если" +
            " из предложенных вариантов специализаций Вам ничего не подходит - свяжитесь с нами напрямую, используя" +
            " команду /contacts. Мы обработаем Ваш запрос в индивидуальном порядке и предложим Вам работу мечты!" +
            " \n\n Если вы хотите посмотреть все активные вакансии от нашей компании, то нажмите \"Смотреть все вакансии\"" +
            " - для Вас будет сформирован список наших незакрытых вакансий с сайта hh.ru";

    public TelegramBot(BotConfig config) {
        this.botConfig = config;
        setBotMenu();
    }

    public String getBotUsername() {
        return botConfig.getBotName();
    }

    public String getBotToken() {
        return botConfig.getBotToken();
    }

    public void onUpdateReceived(Update update) {
        long chatId = 0L;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            chatId = update.getMessage().getChatId();
            switch (userMessage) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommand(chatId);
                    break;
                case "/about":
                    infoCommand(chatId);
                    break;
                case "/job":
                    jobCommand(chatId);
                    break;
                case "/contacts":
                    contactsCommand(chatId);
                    break;
                case "/help":
                    helpCommand(chatId);
                    break;
                case "/statistics":
                    adminCommand(chatId, update.getMessage().getChat().getUserName());
                    break;
                default:
                    String error = "Ваш текст не похож на команду. Обратитесь к главному меню ↙";
                    sendMessage(chatId, error);
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            long chatIdQuery = update.getCallbackQuery().getMessage().getChatId();
            if (data.equals("TELEPHONE_REG") || data.equals("CALL_ME")) {
                sendMessage(chatIdQuery, "\uD83D\uDCDE   Для вызова нажмите +78005551004");
            } else if (data.equals("USER_EMPLOYER")) {
                forEmployer(chatIdQuery);
            } else if (data.equals("USER_EMPLOYEE") || data.equals("BACK")) {
                forEmployee(chatIdQuery);
            } else if (data.equals("OTHER")) {
                forOther(chatIdQuery);
            } else if (data.equals("RETAIL")) {
                forRetail(chatIdQuery);
            } else if (data.equals("CONSTRUCTION")) {
                forConstruction(chatIdQuery);
            } else if (data.equals("PRODUCTION")) {
                forProduction(chatIdQuery);
            } else if (data.equals("AGRO")) {
                forAgro(chatIdQuery);
            } else if (data.equals("LOGISTIC")) {
                forLogistic(chatIdQuery);
            } else if (data.equals("CASHIER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%BA%D0%B0%D1%81%D1%81%D0%B8%D1%80&salary=&ored_clusters=true&search_field=name&employer_id=1966177&hhtmFrom=vacancy_search_list");
            } else if (data.equals("PICKER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9A%D0%BE%D0%BC%D0%BF%D0%BB%D0%B5%D0%BA%D1%82%D0%BE%D0%B2%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&search_field=name&employer_id=1966177&hhtmFrom=vacancy_search_list");
            } else if (data.equals("SLESAR")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D1%81%D0%BB%D0%B5%D1%81%D0%B0%D1%80%D1%8C&salary=&ored_clusters=true&items_on_page=100&search_field=name&employer_id=1966177&hhtmFrom=vacancy_search_list");
            } else if (data.equals("SVARKA")) {
                forDetailVacancy(chatIdQuery, "https://lipetsk.hh.ru/search/vacancy?text=%D0%A1%D0%B2%D0%B0%D1%80%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("RAB_TORG_ZALA")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D0%BD%D0%B8%D0%BA+%D1%82%D0%BE%D1%80%D0%B3%D0%BE%D0%B2%D0%BE%D0%B3%D0%BE+%D0%B7%D0%B0%D0%BB%D0%B0&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("BAKER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%BF%D0%B5%D0%BA%D0%B0%D1%80%D1%8C&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("MERCHANDAIZER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9C%D0%B5%D1%80%D1%87%D0%B0%D0%BD%D0%B4%D0%B0%D0%B9%D0%B7%D0%B5%D1%80&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("CLEANER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D1%83%D0%B1%D0%BE%D1%80%D1%89%D0%B8%D0%BA&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("L_DRIVER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%92%D0%BE%D0%B4%D0%B8%D1%82%D0%B5%D0%BB%D1%8C+%D0%BF%D0%BE%D0%B3%D1%80%D1%83%D0%B7%D1%87%D0%B8%D0%BA%D0%B0&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("ST_KEEPER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9A%D0%BB%D0%B0%D0%B4%D0%BE%D0%B2%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("TANKER")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%B7%D0%B0%D0%BF%D1%80%D0%B0%D0%B2%D1%89%D0%B8%D0%BA&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("GAZOREZ")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%B3%D0%B0%D0%B7%D0%BE%D1%80%D0%B5%D0%B7%D1%87%D0%B8%D0%BA&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("BETON")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%91%D0%B5%D1%82%D0%BE%D0%BD%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("OTDELKA")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9E%D1%82%D0%B4%D0%B5%D0%BB%D0%BE%D1%87%D0%BD%D0%B8%D0%BA&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("PLITKA")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9F%D0%BB%D0%B8%D1%82%D0%BE%D1%87%D0%BD%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("KAMEN")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9A%D0%B0%D0%BC%D0%B5%D0%BD%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("IN_PTO")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%98%D0%BD%D0%B6%D0%B5%D0%BD%D0%B5%D1%80+%D0%BF%D1%82%D0%BE&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("MONTAZH")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9C%D0%BE%D0%BD%D1%82%D0%B0%D0%B6%D0%BD%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("DRIVER_SPEC_TR")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%92%D0%BE%D0%B4%D0%B8%D1%82%D0%B5%D0%BB%D1%8C+%D1%81%D0%BF%D0%B5%D1%86%D1%82%D1%80%D0%B0%D0%BD%D1%81%D0%BF%D0%BE%D1%80%D1%82%D0%B0&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("SHLOFOVSHIK")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A8%D0%BB%D0%B8%D1%84%D0%BE%D0%B2%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("TOKAR")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A2%D0%BE%D0%BA%D0%B0%D1%80%D1%8C&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("ELECTROMONT")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%AD%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%BE%D0%BD%D1%82%D0%B0%D0%B6%D0%BD%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("SBOR_MK")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A1%D0%B1%D0%BE%D1%80%D1%89%D0%B8%D0%BA+%D0%BC%D0%B5%D1%82%D0%B0%D0%BB%D0%BB%D0%BE%D0%BA%D0%BE%D0%BD%D1%81%D1%82%D1%80%D1%83%D0%BA%D1%86%D0%B8%D0%B8&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("ZATOCHNIK")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%97%D0%B0%D1%82%D0%BE%D1%87%D0%BD%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("SVERLO")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A1%D0%B2%D0%B5%D1%80%D0%BB%D0%BE%D0%B2%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("OPERATOR_CHPU")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9E%D0%BF%D0%B5%D1%80%D0%B0%D1%82%D0%BE%D1%80+%D1%87%D0%BF%D1%83&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("GRUZ")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%93%D1%80%D1%83%D0%B7%D1%87%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("RAZNORAB")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A0%D0%B0%D0%B7%D0%BD%D0%BE%D1%80%D0%B0%D0%B1%D0%BE%D1%87%D0%B8%D0%B9&from=suggest_post&salary=&ored_clusters=true&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("BRIGADIR")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%B1%D1%80%D0%B8%D0%B3%D0%B0%D0%B4%D0%B8%D1%80&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("FASOVKA")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?from=suggest_post&ored_clusters=true&area=113&hhtmFrom=vacancy_search_list&search_field=name&search_field=company_name&text=%D0%A4%D0%B0%D1%81%D0%BE%D0%B2%D1%89%D0%B8%D0%BA&enable_snippets=false&employer_id=1966177");
            } else if (data.equals("UKLADKA")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A3%D0%BA%D0%BB%D0%B0%D0%B4%D1%87%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("STIKEROV")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A1%D1%82%D0%B8%D0%BA%D0%B5%D1%80%D0%BE%D0%B2%D1%89%D0%B8%D0%BA&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("DRIVER_RICH")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%92%D0%BE%D0%B4%D0%B8%D1%82%D0%B5%D0%BB%D1%8C+%D1%80%D0%B8%D1%87%D1%82%D1%80%D0%B0%D0%BA%D0%B0&from=suggest_post&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("MOIKA")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9C%D0%BE%D0%B9%D1%89%D0%B8%D0%BA&from=suggest_post&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("SKOT")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D1%81%D0%BA%D0%BE%D1%82%D0%BD%D0%B8%D0%BA&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("OBVAL")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%BE%D0%B1%D0%B2%D0%B0%D0%BB%D1%8C%D1%89%D0%B8%D0%BA&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("ZABOI_SKOT")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%B7%D0%B0%D0%B1%D0%BE%D0%B9%D1%89%D0%B8%D0%BA+%D1%81%D0%BA%D0%BE%D1%82%D0%B0&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("PODSOB_RAB")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%BF%D0%BE%D0%B4%D1%81%D0%BE%D0%B1%D0%BD%D1%8B%D0%B9+%D1%80%D0%B0%D0%B1%D0%BE%D1%87%D0%B8%D0%B9&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("SBORKA_KOR")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%A1%D0%B1%D0%BE%D1%80%D1%89%D0%B8%D0%BA+%D0%BA%D0%BE%D1%80%D0%BE%D0%B1%D0%BE%D0%B2&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            } else if (data.equals("OPERATOR_SVIN_K")) {
                forDetailVacancy(chatIdQuery, "https://hh.ru/search/vacancy?text=%D0%9E%D0%BF%D0%B5%D1%80%D0%B0%D1%82%D0%BE%D1%80+%D1%81%D0%B2%D0%B8%D0%BD%D0%BE%D0%B2%D0%BE%D0%B4%D1%87%D0%B5%D1%81%D0%BA%D0%BE%D0%B3%D0%BE+%D0%BA%D0%BE%D0%BC%D0%BF%D0%BB%D0%B5%D0%BA%D1%81%D0%B0&from=suggest_post&salary=&ored_clusters=true&search_field=name&search_field=company_name&employer_id=1966177&area=113&hhtmFrom=vacancy_search_list");
            }
        } else {
            String error = "Упс! Что-то пошло не так. Попробуйте позднее";
            sendMessage(chatId, error);
        }
    }

    private void setBotMenu() {
        List<BotCommand> listMenu = new ArrayList<>();
        listMenu.add(new BotCommand("/start", "Запуск бота"));
        listMenu.add(new BotCommand("/about", "Информация о деятельности компании"));
        listMenu.add(new BotCommand("/job", "Раздел вакансий (для работодателей и соискателей)"));
        listMenu.add(new BotCommand("/contacts", "Контакты"));
        listMenu.add(new BotCommand("/help", "Помощь"));
        listMenu.add(new BotCommand("/statistics", "Статистика (только для администратора)"));
        try {
            this.execute(new SetMyCommands(listMenu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    private void registerUser(Message message) {
        if (!userRepo.existsUserByChatId(message.getChatId())) {
            Long chatId = message.getChatId();
            Chat chat = message.getChat();
            User user = new User(chatId, chat.getFirstName(),
                    chat.getLastName(), chat.getUserName(), new Timestamp(System.currentTimeMillis()));

            userRepo.save(user);
        }
    }

    private void adminCommand(long chatId, String userName) {
        if (userName.equals(botConfig.getBotAdmin())) {
            List<User> usersCount = userRepo.findAll();
            sendMessage(chatId, "Количество пользователей: " + usersCount.size());
            for (User user : usersCount) {
                sendMessage(chatId, user.toString());
            }
        } else {
            sendMessage(chatId, "Статистика доступна только администратору");
        }
    }

    private void sendMessage(long chatId, String sendMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(sendMessage);
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            ex.getMessage();
        }
    }

    private void sendMessage(long chatId, String sendMessage, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(sendMessage);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            ex.getMessage();
        }
    }

    private void startCommand(long chatId) {
        String url = "https://vk.com/buisness.process?z=photo-59509628_457243549%2Falbum-59509628_0%2Frev";
        StringBuilder builder = new StringBuilder();
        builder = builder.append("\uD83D\uDC65 Вас приветствует бот ГК \"Бизнес Процесс\"")
                .append(" \n\n В зависимости от запроса мы предлагаем услуги по полному")
                .append(" или частичному процессу подбора и трудоустройства кандидатов, а соискателям мы гарантируем")
                .append(" стабильную работу в кратчайшие сроки с удобным графиком и приятными условиями.")
                .append("\n\n Чтобы узнать более подробно интересующую Вас информацию,")
                .append(" воспользутесь нашим меню слева от формы ввода сообщения ↙");
        getImage(chatId, url, builder.toString());
    }

    private void infoCommand(long chatId) {
        String url = "https://vk.com/buisness.process";
        StringBuilder builder = new StringBuilder();
        builder = builder.append("ℹ \"Бизнес Процесс\" в цифрах: 15 лет опыта, 42 региона с вакансиями и более 800 открытых объявлений о работе.")
                .append(" \n\n Ежедневно более 5 000 сотрудников выходят на работу в \"Бизнес Процесс\" по всей России.")
                .append(" Наши постоянные клиенты: \n ▫ Ашан \n ▫ Лукойл \n ▫ Полисан \n ▫ Росатом \n ▫ Северная верфь \n ▫ БФА Девелопмент \n ▫ другие крупнейшие компании страны \uD83D\uDD1D")
                .append("\n\n Мы оказываем такие услуги, как: \n ✔ рекрутмент \n ✔ аутсорсинг \n ✔ трудоустройство соискателя")
                .append("\n\n Если Вас заинтересовала одна из оказываемых нами услуг, то Вы можете связаться с нами, используя")
                .append(" команду /contacts, либо перейти на наш официальный сайт за более подробной информацией ⬇");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> listMarkup = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine1 = new ArrayList<>();

        var buttonSite = new InlineKeyboardButton();

        buttonSite.setText("Официальный сайт ГК Бизнес Процесс \uD83D\uDC65 \n\n");
        buttonSite.setCallbackData("SITE_GK_BP");
        buttonSite.setUrl("https://spboutsourcing.ru/");

        rowsInLine1.add(buttonSite);
        listMarkup.add(rowsInLine1);

        markup.setKeyboard(listMarkup);

        getImage(chatId, url, builder.toString(), markup);
    }

    private void jobCommand(long chatId) {
        String answer = "Вы переходите в раздел с вакансиями, но для начала нам необходимо уточнить, являетесь Вы" +
                " работодателем или соискателем?";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> listMarkup = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine2 = new ArrayList<>();

        var buttonEmployer = new InlineKeyboardButton();
        var buttonEmployee = new InlineKeyboardButton();

        buttonEmployer.setText("Работодатель");
        buttonEmployer.setCallbackData("USER_EMPLOYER");
        buttonEmployee.setText("Соискатель");
        buttonEmployee.setCallbackData("USER_EMPLOYEE");

        rowsInLine1.add(buttonEmployer);
        rowsInLine2.add(buttonEmployee);
        listMarkup.add(rowsInLine1);
        listMarkup.add(rowsInLine2);
        markup.setKeyboard(listMarkup);

        sendMessage(chatId, answer, markup);
    }

    private void contactsCommand(long chatId) {
        String answer = "Вы можете связаться с нашими сотрудниками одним из следующих способов:" +
                "\n\n ▫ написать в Telegram \n ▫ написать в VK \n ▫ позвонить по телефону" +
                "\n\n Мы оперативно и полно ответим на любые Ваши вопросы, вне зависимости от выбранного Вами средства связи!" +
                "\n\n \uD83C\uDFE2 Наш адрес: г. Санкт-Петербург, Владимирский проспект 17 | корпус 1 | этаж 5" +
                "\n \uD83D\uDCC5 Режим работы: будние дни с 9:00 до 18:00";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> listMarkup = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine2 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine3 = new ArrayList<>();

        var buttonTG = new InlineKeyboardButton();
        var buttonVK = new InlineKeyboardButton();
        var buttonCall = new InlineKeyboardButton();

        buttonTG.setText("Написать в TG ✎");
        buttonTG.setCallbackData("WRITE_TG");
        buttonTG.setUrl("t.me/Iam");
        buttonVK.setText("Написать в VK ✍");
        buttonVK.setCallbackData("WRITE_VK");
        buttonVK.setUrl("https://vk.com/im?media=&sel=-59509628");
        buttonCall.setText("Позвонить \uD83D\uDCDE");
        buttonCall.setCallbackData("CALL_ME");

        rowsInLine1.add(buttonTG);
        rowsInLine2.add(buttonVK);
        rowsInLine3.add(buttonCall);

        listMarkup.add(rowsInLine1);
        listMarkup.add(rowsInLine2);
        listMarkup.add(rowsInLine3);
        markup.setKeyboard(listMarkup);

        sendMessage(chatId, answer, markup);
    }

    private void helpCommand(long chatId) {
        String answer = "\uD83D\uDD0D Если Вы не нашли необходимую информацию, то воспользуйтесь командой /contacts и " +
                "свяжитесь с нашими сотрудниками одним из указанных способов. Мы ответим на любой Ваш вопрос!";
        sendMessage(chatId, answer);
    }

    private void forEmployer(long chatId) {
        String zayavka = "https://spboutsourcing.ru/#zayavka";
        String uslugi = "https://spboutsourcing.ru/#uslugi";
        String answer = " \uD83D\uDCDD Мы можем предложить Вам заполнить нужную форму заявки с учетом выбранной Вами услуги: " +
                "аутсоринг, рекрутмент или Ваш вариант подбора персонала. \n\n Также Вы можете предварительно заполнить" +
                " форму расчета примерной стоимости наших услуг аутсорсинга, если у Вас есть такая необходимость";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> listMarkup = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine2 = new ArrayList<>();

        var buttonZayavka = new InlineKeyboardButton();
        var buttonUslugi = new InlineKeyboardButton();

        buttonZayavka.setText("Заполнить заявку  \uD83D\uDD8A");
        buttonZayavka.setCallbackData("ZAYAVKA");
        buttonZayavka.setUrl(uslugi);
        buttonUslugi.setText("Получить расчет стоимости  \uD83D\uDCB2");
        buttonUslugi.setCallbackData("USLUGI");
        buttonUslugi.setUrl(zayavka);

        rowsInLine1.add(buttonZayavka);
        rowsInLine2.add(buttonUslugi);

        listMarkup.add(rowsInLine1);
        listMarkup.add(rowsInLine2);
        markup.setKeyboard(listMarkup);

        sendMessage(chatId, answer, markup);
    }

    private void forEmployee(long chatId) {
        String answer = "\uD83D\uDD0E Для того, чтобы мы смогли предоставить наиболее подходящие Вам вакансии, выберите" +
                " нужную Вам отрасль из списка ниже:";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> listMarkup = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine2 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine3 = new ArrayList<>();

        var buttonRetail = new InlineKeyboardButton();
        var buttonConstruction = new InlineKeyboardButton();
        var buttonProduction = new InlineKeyboardButton();
        var buttonLogistic = new InlineKeyboardButton();
        var buttonAgro = new InlineKeyboardButton();
        var buttonOther = new InlineKeyboardButton();

        buttonRetail.setText("Ритейл");
        buttonRetail.setCallbackData("RETAIL");
        buttonConstruction.setText("Строительство");
        buttonConstruction.setCallbackData("CONSTRUCTION");
        buttonProduction.setText("Производство");
        buttonProduction.setCallbackData("PRODUCTION");
        buttonLogistic.setText("Складская логистика");
        buttonLogistic.setCallbackData("LOGISTIC");
        buttonAgro.setText("Агрокомплексы");
        buttonAgro.setCallbackData("AGRO");
        buttonOther.setText("Другое");
        buttonOther.setCallbackData("OTHER");

        rowsInLine1.add(buttonRetail);
        rowsInLine1.add(buttonConstruction);
        rowsInLine2.add(buttonProduction);
        rowsInLine2.add(buttonLogistic);
        rowsInLine3.add(buttonAgro);
        rowsInLine3.add(buttonOther);

        listMarkup.add(rowsInLine1);
        listMarkup.add(rowsInLine2);
        listMarkup.add(rowsInLine3);
        markup.setKeyboard(listMarkup);

        sendMessage(chatId, answer, markup);
    }

    private InlineKeyboardMarkup setMarkup(String vakancy1, String callbackData1,
                                           String vakancy2, String callbackData2,
                                           String vakancy3, String callbackData3,
                                           String vakancy4, String callbackData4,
                                           String vakancy5, String callbackData5,
                                           String vakancy6, String callbackData6,
                                           String vakancy7, String callbackData7,
                                           String vakancy8, String callbackData8,
                                           String vakancy9, String callbackData9) {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> listMarkup = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine2 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine3 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine4 = new ArrayList<>();
        List<InlineKeyboardButton> rowsInLine5 = new ArrayList<>();

        var button1 = new InlineKeyboardButton();
        var button2 = new InlineKeyboardButton();
        var button3 = new InlineKeyboardButton();
        var button4 = new InlineKeyboardButton();
        var button5 = new InlineKeyboardButton();
        var button6 = new InlineKeyboardButton();
        var button7 = new InlineKeyboardButton();
        var button8 = new InlineKeyboardButton();
        var button9 = new InlineKeyboardButton();
        var buttonAllVacancy = new InlineKeyboardButton();
        var buttonBack = new InlineKeyboardButton();

        button1.setText(vakancy1);
        button1.setCallbackData(callbackData1);
        button2.setText(vakancy2);
        button2.setCallbackData(callbackData2);
        button3.setText(vakancy3);
        button3.setCallbackData(callbackData3);
        button4.setText(vakancy4);
        button4.setCallbackData(callbackData4);
        button5.setText(vakancy5);
        button5.setCallbackData(callbackData5);
        button6.setText(vakancy6);
        button6.setCallbackData(callbackData6);
        button7.setText(vakancy7);
        button7.setCallbackData(callbackData7);
        button8.setText(vakancy8);
        button8.setCallbackData(callbackData8);
        button9.setText(vakancy9);
        button9.setCallbackData(callbackData9);
        buttonAllVacancy.setText("Смотреть все вакансии ✅");
        buttonAllVacancy.setCallbackData("ALL_VACANCY");
        buttonAllVacancy.setUrl(ALL_VAKANCIES);
        buttonBack.setText("<< Назад");
        buttonBack.setCallbackData("BACK");

        rowsInLine1.add(button1);
        rowsInLine1.add(button2);
        rowsInLine1.add(button3);
        rowsInLine2.add(button4);
        rowsInLine2.add(button5);
        rowsInLine2.add(button6);
        rowsInLine3.add(button7);
        rowsInLine3.add(button8);
        rowsInLine3.add(button9);
        rowsInLine4.add(buttonAllVacancy);
        rowsInLine5.add(buttonBack);

        listMarkup.add(rowsInLine1);
        listMarkup.add(rowsInLine2);
        listMarkup.add(rowsInLine3);
        listMarkup.add(rowsInLine4);
        listMarkup.add(rowsInLine5);
        markup.setKeyboard(listMarkup);

        return markup;

    }

    private void forRetail(long chatId) {
        sendMessage(chatId, ANSWER_FOR_ALL, setMarkup("Кассир", "CASHIER",
                "Работник торгового зала", "RAB_TORG_ZALA", "Пекарь", "BAKER",
                "Мерчандайзер", "MERCHANDAIZER", "Уборщик", "CLEANER",
                "Водитель погрузчика", "L_DRIVER", "Комплектовщик", "PICKER",
                "Кладовщик", "ST_KEEPER", "Заправщик", "TANKER"));
    }

    private void forConstruction(long chatId) {
        sendMessage(chatId, ANSWER_FOR_ALL, setMarkup("Газорезчик", "GAZOREZ", "Бетонщик",
                "BETON", "Отделочник", "OTDELKA", "Плиточник", "PLITKA",
                "Каменщик", "KAMEN", "Инженер ПТО", "IN_PTO", "Сварщик",
                "SVARKA", "Монтажник", "MONTAZH", "Водитель спецтранспорта",
                "DRIVER_SPEC_TR"));
    }

    private void forProduction(long chatId) {
        sendMessage(chatId, ANSWER_FOR_ALL, setMarkup("Слесарь", "SLESAR", "Шлифовщик",
                "SHLOFOVSHIK", "Сварщик", "SVARKA", "Токарь",
                "TOKAR", "Электромонтажник", "ELECTROMONT", "Сборщик МК",
                "SBOR_MK", "Заточник", "ZATOCHNIK", "Сверловщик", "SVERLO",
                "Оператор ЧПУ", "OPERATOR_CHPU"));

    }

    private void forLogistic(long chatId) {
        sendMessage(chatId, ANSWER_FOR_ALL, setMarkup("Грузчик", "GRUZ", "Разнорабочий",
                "RAZNORAB", "Комплектовщик", "PICKER", "Бригадир", "BRIGADIR",
                "Фасовщик", "FASOVKA", "Укладчик", "UKLADKA", "Стикеровщик",
                "STIKEROV", "Водитель ричтрака", "DRIVER_RICH", "Уборщик", "CLEANER"));
    }

    private void forAgro(long chatId) {
        sendMessage(chatId, ANSWER_FOR_ALL, setMarkup("Мойщик", "MOIKA", "Разнорабочий",
                "RAZNORAB", "Скотник", "SKOT", "Обвальщик", "OBVAL",
                "Забойщик скота", "ZABOI_SKOT", "Бригадир", "BRIGADIR",
                "Подсобный рабочий", "PODSOB_RAB", "Сборщик коробов", "SBORKA_KOR",
                "Оператор свиноводческого комплеса", "OPERATOR_SVIN_K"));
    }

    //один метод для каждой отдельной специализации
    private void forDetailVacancy(long chatId, String url) {
        List<String> newLinks = HtmlParser.parsingUrl(url);
        if (newLinks.isEmpty()) {
            sendMessage(chatId, "По вашему запросу вакансий не найдено \uD83D\uDE41");
        } else {
            for (String link : newLinks) {
                sendMessage(chatId, "По вашему запросу найдена вакансия: " + link);
            }
        }
    }

    private void forOther(long chatId) {
        helpCommand(chatId);
    }

    private void getImage(long chatId, String imageUrl, String text) {
        try {
            URL url = new URL(imageUrl);
            InputFile photo = new InputFile(String.valueOf(url));
            SendPhoto sPhoto = new SendPhoto();
            sPhoto.setPhoto(photo);
            sPhoto.setChatId(String.valueOf(chatId));
            sPhoto.setCaption(text);
            execute(sPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getImage(long chatId, String imageUrl, String text, InlineKeyboardMarkup markup) {
        try {
            URL url = new URL(imageUrl);
            InputFile photo = new InputFile(String.valueOf(url));
            SendPhoto sPhoto = new SendPhoto();
            sPhoto.setPhoto(photo);
            sPhoto.setChatId(String.valueOf(chatId));
            sPhoto.setCaption(text);
            sPhoto.setReplyMarkup(markup);
            execute(sPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}