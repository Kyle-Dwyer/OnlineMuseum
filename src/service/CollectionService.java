package service;

import bean.Collection;
import dao.CollectionDao;

/**
 * @author: jiaxing liu
 * @Date: 2019/7/17 23:46
 */
public class CollectionService {
    private CollectionDao collectionDao;

    public void init() {
        collectionDao = new CollectionDao();
        collectionDao.init();
    }

    public void destroy() {
        collectionDao.destroy();
    }

    private boolean save(Collection collection) {
        return collectionDao.save(collection);
    }

    public boolean save(int userID, int itemID) {
        return save(new Collection(userID, itemID));
    }

    private boolean delete(Collection collection) {
        return collectionDao.delete(collection);
    }

    public boolean delete(int userID, int itemID) {
        return delete(new Collection(userID, itemID));
    }

    public boolean update(int userID, int itemID) {
        return update(new Collection(userID, itemID));
    }

    private boolean update(Collection collection) {
        return collectionDao.update(collection);
    }

    public boolean getPermission(Collection collection) {
        return collectionDao.getPermission(collection);
    }

}
