package dao;

import service.Item;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author: jiaxing liu
 * @Date: 2019/7/16 23:42
 */
public class CollectionDao {
    private Connection connection;
    public void init(){
        connection = DAOHelper.getConnection();
    }

    public void destroy() {
        if (connection!= null) {
            connection= null;
        }
    }

    public boolean save(Item item){
        return true;
    }

    public ArrayList<Item> getItems(int userID) {
        ArrayList<Item> items = new ArrayList<>();
        ArrayList<Integer> itemIDs = new ArrayList<>();
        String sql = "select * from useritem where userID=" + userID;
        ResultSet rs=null;
        try {
            Statement statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            if(rs.next()){
                int itemID = rs.getInt("itemID");
                itemIDs.add(itemID);
            }
            statement.close();
            rs.close();

            for(Integer id: itemIDs) {
                items.add(getItem(id));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return items;
    }

    public Item getItem(int itemID) {
        String sql = "select * from items where id=";
        Item item = new Item();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql+ itemID);
            if(rs.next()){
                int id = rs.getInt("id");
                String name =  rs.getString("name");
                String img = rs.getString("img");
                String description = rs.getString("description");
                String video = rs.getString("video");
                int hot = rs.getInt("id");
                String time = rs.getString("time");
                String location = rs.getString("location");
                item = new Item(id,name,img,description,video,hot,time,location);
            }
            statement.close();
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return item;
    }

}
