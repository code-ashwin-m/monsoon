package org.ashwin.monsoon.db.test1;

import org.ashwin.monsoon.core.annotations.Inject;
import org.ashwin.monsoon.core.annotations.Service;

@Service
public class TestService {
    @Inject
    private TestRepository repository;

    public void createTableIfNotExists() {
        repository.createTableIfNotExists();
    }

    public Object create(TestEntity entity) {
        return repository.create(entity);
    }
}
