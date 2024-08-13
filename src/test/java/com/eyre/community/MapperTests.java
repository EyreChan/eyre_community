package com.eyre.community;

import com.eyre.community.dao.DiscussPostMapper;
import com.eyre.community.dao.UserMapper;
import com.eyre.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = EyreApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }
//
//    @Test
//    public void testSelectPosts() {
//        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
//        for(DiscussPost post : list) {
//            System.out.println(post);
//        }
//
//        int rows = discussPostMapper.selectDiscussPostRows(149);
//        System.out.println(rows);
//    }
}
