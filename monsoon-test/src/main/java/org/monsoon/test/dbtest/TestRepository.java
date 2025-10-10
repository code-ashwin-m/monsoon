package org.monsoon.test.dbtest;

import org.monsoon.framework.db.interfaces.BaseRepository;
import org.monsoon.framework.db.annotations.Repository;

import java.util.List;

@Repository(entity = TestUserEntity.class)
public interface TestRepository extends BaseRepository<TestUserEntity> {
    List<TestUserEntity> findByEmail(String email);
}
