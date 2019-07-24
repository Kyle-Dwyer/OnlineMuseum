package controller;

import bean.Item;
import bean.SearchResult;
import com.alibaba.fastjson.JSON;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import service.ItemService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员管理展品的servlet类
 * doGet获取所有展品的信息
 * doPost对展品进行增删改的操作
 *
 */
public class ManageItemsServlet extends HttpServlet {
    private ItemService itemService;

    public void init() {
        itemService = new ItemService();
        itemService.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer user_permission = (Integer) req.getSession().getAttribute("permission");
        if(user_permission == null || user_permission != 2){
            resp.setStatus(401);
            return;
        }
        //利用之前写的搜索的接口，无查询键无排序获得所有展品，包装一下为原来的接口增添一个allpage方便拿到所有信息
        SearchResult searchResult = itemService.getItemsByOrder("", "", 1, true);
        if (searchResult.getItems() != null) {
            resp.setStatus(200);
            OutputStream out = resp.getOutputStream();
            out.write(JSON.toJSONString(searchResult).getBytes(StandardCharsets.UTF_8));
        } else {
            resp.setStatus(400);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer user_permission = (Integer) req.getSession().getAttribute("permission");
        if(user_permission == null || user_permission != 2){
            resp.setStatus(401);
            return;
        }
        //判断post的类型，multypart/form-data不能用getParameter获取参数
        String contentType = req.getContentType();
        //创建一个map存储属性名和属性值
        Map<String, String> info = new HashMap<>();
        //为了避免文件重复，如果用户有上传文件使用随机文件名
        String randomImgName = null;
        String randomVideoName = null;
        if (contentType != null && contentType.contains("multipart/form-data")) {
            //处理前台的图片上传操作
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //设置缓存路径
            factory.setRepository(new File(req.getServletContext().getRealPath("/templates/img/temp")));
            ServletFileUpload upload = new ServletFileUpload(factory);
            //解决文件名包含中文乱码问题
            upload.setHeaderEncoding("UTF-8");
            try {
                List<FileItem> fileItems = upload.parseRequest(req);
                for (FileItem item : fileItems) {
                    if (item.isFormField()) {//文本输入记录键值对
                        String name = item.getFieldName();
                        String value = item.getString();
                        info.put(name, value);
                    } else {//文件输入
                        //获取文件名和表项名
                        String filename = item.getName();
                        String fieldname = item.getFieldName();
                        if (filename == null || filename.equals(""))
                            continue;
                        //利用UUID生成伪随机字符串，作为文件名避免重复
                        String uuid = UUID.randomUUID().toString();
                        //获取文件后缀名
                        String suffix = filename.substring(filename.lastIndexOf("."));
                        String uploadPath = "";
                        String realFilename = "";
                        if (fieldname.equals("imgFile")) {//上传图片
                            uploadPath = req.getServletContext().getRealPath("/templates/img/art_img");
                            File file = new File(uploadPath);
                            file.mkdirs();
                            randomImgName = uuid + suffix;
                            realFilename = randomImgName;
                        } else if (fieldname.equals("videoFile")) {//上传视频
                            uploadPath = req.getServletContext().getRealPath("/templates/videos");
                            File file = new File(uploadPath);
                            file.mkdirs();
                            randomVideoName = uuid + suffix;
                            realFilename = randomVideoName;
                        } else {
                            continue;
                        }
                        item.write(new File(uploadPath, realFilename));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //一般表单用getParameter获取参数
            info.put("operation", req.getParameter("operation"));

        }
        String operation = info.get("operation");
        switch (operation) {
            case "add"://增
                //获取所有展品输入信息，以下格式为了避免中文乱码
                String name = new String(info.get("name").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String img = randomImgName != null ? randomImgName : new String(info.get("img").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                img = img.contains("\\") ? img.substring(img.lastIndexOf("\\") + 1) : img;
                img = img.contains("/") ? img.substring(img.lastIndexOf("/") + 1) : img;
                String description = new String(info.get("description").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String video = randomVideoName != null ? randomVideoName : new String(info.get("video").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                video = video.contains("\\") ? video.substring(video.lastIndexOf("\\") + 1) : video;
                video = video.contains("/") ? video.substring(video.lastIndexOf("/") + 1) : video;
                int hot = Integer.parseInt(new String(info.get("hot").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                int time = Integer.parseInt(new String(info.get("time").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                String location = new String(info.get("location").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String genre = new String(info.get("genre").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                //储存
                if (itemService.save(new Item(name, img, description, video, hot, time, location, genre))) {
                    resp.setStatus(200);
                } else {
                    resp.setStatus(401);
                }
                break;
            case "change"://改
                //获取所有展品输入信息，以下格式为了避免中文乱码
                int id = Integer.parseInt(info.get("id"));
                String name2 = new String(info.get("name").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String img2 = randomImgName != null ? randomImgName : new String(info.get("img").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                img2 = img2.substring(img2.lastIndexOf("\\") + 1);
                img2 = img2.contains("\\") ? img2.substring(img2.lastIndexOf("\\") + 1) : img2;
                img2 = img2.contains("/") ? img2.substring(img2.lastIndexOf("/") + 1) : img2;
                String description2 = new String(info.get("description").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String video2 = randomVideoName != null ? randomVideoName : new String(info.get("video").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                video2 = video2.contains("\\") ? video2.substring(video2.lastIndexOf("\\") + 1) : video2;
                video2 = video2.contains("/") ? video2.substring(video2.lastIndexOf("/") + 1) : video2;
                int hot2 = Integer.parseInt(new String(info.get("hot").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                int time2 = Integer.parseInt(new String(info.get("time").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                String location2 = new String(info.get("location").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String genre2 = new String(info.get("genre").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                //修改
                if (itemService.updateItem(new Item(id, name2, img2, description2, video2, hot2, time2, location2, genre2))) {
                    resp.setStatus(200);
                } else {
                    resp.setStatus(401);
                }
                break;
            case "delete"://删
                int id2 = Integer.parseInt(req.getParameter("id"));
                if (itemService.deleteItem(id2)) {
                    resp.setStatus(200);
                } else {
                    resp.setStatus(401);
                }
                break;
        }
    }
}
