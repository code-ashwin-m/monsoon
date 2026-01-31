package org.monsoon.sample;

import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Service;

@Service
public class UserService {
    private UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
        userRepo.createTableIfNotExists();
    }

    public void create(UserDto userDto) {
        User user = GenericMapper.map(userDto, User.class);
        userRepo.create(user);
    }
    
}
