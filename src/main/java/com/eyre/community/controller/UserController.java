package com.eyre.community.controller;

import com.eyre.community.annotation.LoginRequired;
import com.eyre.community.entity.Comment;
import com.eyre.community.entity.DiscussPost;
import com.eyre.community.entity.Page;
import com.eyre.community.entity.User;
import com.eyre.community.service.*;
import com.eyre.community.util.CommunityConstant;
import com.eyre.community.util.CommunityUtil;
import com.eyre.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        logger.info("随机文件名：" + fileName);

        try {
            // 确定文件存放的路径
            String filePath = CommunityUtil.upload(headerImage.getBytes(), fileName);

            // 更新当前用户的头像的路径(web访问路径)
            User user = hostHolder.getUser();
            userService.updateHeader(user.getId(), filePath);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        return "redirect:/index";
    }

    // 废弃
    // 解析网络地址，读取本地存储头像
//    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
//    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
//        // 服务器存放路径
//        fileName = uploadPath + "/" + fileName;
//        // 文件后缀
//        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        // 响应图片
//        response.setContentType("image/" + suffix);
//        try (
//                OutputStream os = response.getOutputStream();
//                FileInputStream fis = new FileInputStream(fileName);
//        ) {
//            byte[] buffer = new byte[1024];
//            int b = 0;
//            while ((b = fis.read(buffer)) != -1) {
//                os.write(buffer, 0, b);
//            }
//        } catch (IOException e) {
//            logger.error("读取头像失败: " + e.getMessage());
//        }
//    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    @RequestMapping(path = "/posts/{userId}", method = RequestMethod.GET)
    public String getPostPage(@PathVariable("userId") int userId, Page page, Model model) {
        int postRows = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("postRows", postRows);

        page.setLimit(5);
        page.setRows(postRows);
        page.setPath("/user/posts/" + userId);
        List<DiscussPost> posts = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);

        List<Map<String, Object>> postVOlist = new ArrayList<>();
        for (DiscussPost post : posts) {
            Map<String, Object> postVO = new HashMap<>();
            postVO.put("post", post);
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
            postVO.put("likeCount", likeCount);

            postVOlist.add(postVO);
        }
        model.addAttribute("posts", postVOlist);

        return "/site/my-post";
    }

    @RequestMapping(path = "/comments/{userId}", method = RequestMethod.GET)
    public String getCommentPage(@PathVariable("userId") int userId, Page page, Model model) {
        int commentRows = commentService.findCommentCountByUser(userId);
        model.addAttribute("commentRows", commentRows);

        page.setLimit(5);
        page.setRows(commentRows);
        page.setPath("/user/comments/" + userId);
        List<Comment> comments = commentService.findCommentsByUser(userId, page.getOffset(), page.getLimit());

        List<Map<String, Object>> commentVOlist = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String, Object> commentVO = new HashMap<>();
            commentVO.put("comment", comment);
            DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
            commentVO.put("title", discussPost.getTitle());
            commentVOlist.add(commentVO);
        }
        model.addAttribute("comments", commentVOlist);

        return "/site/my-reply";
    }
}
