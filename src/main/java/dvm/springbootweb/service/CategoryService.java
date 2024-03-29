package dvm.springbootweb.service;

import dvm.springbootweb.entity.Book;
import dvm.springbootweb.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    List<Category> findCategories(int limit);
    List<Category> findAll();
    Category findById(int id);
    Set<Book> getAllBooks(int id);
    void save(Category category);
    void delete(int id);
    boolean checkBookExist(int id);
}
