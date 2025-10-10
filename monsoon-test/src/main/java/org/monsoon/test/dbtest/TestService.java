package org.monsoon.test.dbtest;

import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Service;

import java.util.ArrayList;
import java.util.List;

@Service( name = "service")
public class TestService {
    private TestRepository repository;

    @Autowired
    public TestService(TestRepository repository) {
        this.repository = repository;
        this.repository.createTableIfNotExists();
    }

//    public Object doSomething(){
//        TestUserEntity user1 = new TestUserEntity(null,"ashwin4", "mavila4", "ashwin4@gmail.com");
//        TestUserEntity user2 = new TestUserEntity(null,"ashwin5", "mavila5", "ashwin5@gmail.com");
//
//        List<TestUserEntity> userEntities = new ArrayList<>();
//        userEntities.add(user1);
//        userEntities.add(user2);
//
//        return repository.createMany(userEntities);
//    }

    public boolean createOne() {
        TestUserEntity user1 = new TestUserEntity(null,"ashwin1", "mavila1", "ashwin1@gmail.com");
        return repository.create(user1);
    }

    public boolean createMany() {
        TestUserEntity user1 = new TestUserEntity(null,"ashwin2", "mavila2", "ashwin2@gmail.com");
        TestUserEntity user2 = new TestUserEntity(null,"ashwin3", "mavila3", "ashwin3@gmail.com");
        List<TestUserEntity> userEntities = new ArrayList<>();
        userEntities.add(user1);
        userEntities.add(user2);
        return repository.createMany(userEntities);
    }

    public void update(){
        TestUserEntity user1 = new TestUserEntity("0ff8a22e-67b0-4a1b-83c5-39c17ea51877","kukku4", "mavila4", "kukku4@gmail.com");
        TestUserEntity user2 = new TestUserEntity("2f85e387-d2c2-4457-ba10-91ba3d222deb","kukku3", "mavila3", "kukku3@gmail.com");
        List<TestUserEntity> userEntities = new ArrayList<>();
        userEntities.add(user1);
        userEntities.add(user2);
        repository.updateMany(userEntities);
    }

    public void delete(){

    }

    public void findAll(){
        List<TestUserEntity> users = repository.findAll();
        System.out.println(users);
        repository.deleteMany(users);
    }

    public void findById(){
        TestUserEntity user = repository.findById("1555aabd-688d-494b-abda-a1120786f997");
        System.out.println(user);
    }


    public void findUser() {
        List<TestUserEntity> users = repository.findByEmail("ashwin1@gmail.com");
        if (users.isEmpty()) {
            System.out.println("No user found");
            return;
        }
        TestUserEntity user = users.get(0);
        System.out.println(user);
    }

    public void findUserByName() {
        TestUserEntity user = repository.findUserByFirstAndLastName("ashwin2", "mavila2");
        if (user == null) {
            System.out.println("No user found");
            return;
        }
        System.out.println(user);
    }
}
