package org.monsoon.test.dbtest;

import org.monsoon.framework.db.interfaces.BaseRepository;
import org.monsoon.framework.db.annotations.Repository;

@Repository(entity = TestUserEntity.class)
public interface TestRepository extends BaseRepository<TestUserEntity> {
    String doSomething();
}
