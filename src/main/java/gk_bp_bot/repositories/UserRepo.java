package gk_bp_bot.repositories;

import gk_bp_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository <User, Long> {

    boolean existsUserByChatId(Long chatId);

    @Override
    List<User> findAll();
}
