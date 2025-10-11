package org.monsoon.example.repo;

import org.monsoon.example.entities.User;
import org.monsoon.framework.db.annotations.Repository;
import org.monsoon.framework.db.interfaces.BaseRepository;

@Repository(entity = User.class)
public interface UserRepo extends BaseRepository<User> {
}
