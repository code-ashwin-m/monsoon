package org.monsoon.sample;

import java.util.List;

import org.monsoon.framework.db.annotations.Query;
import org.monsoon.framework.db.annotations.Repository;
import org.monsoon.framework.db.interfaces.BaseRepository;

@Repository(entity = User.class)
public interface UserRepo extends BaseRepository<User> {
    @Query("SELECT * FROM {table} WHERE name = ?")
    List<User> findByUserName(String name);
}
