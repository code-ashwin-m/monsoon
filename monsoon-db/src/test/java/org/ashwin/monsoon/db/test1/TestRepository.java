package org.ashwin.monsoon.db.test1;

import org.ashwin.monsoon.db.annotations.Repository;
import org.ashwin.monsoon.db.interfaces.BaseRepository;

@Repository( entity = TestEntity.class )
public interface TestRepository extends BaseRepository<TestEntity> {
}
