package ua.ivan.epam.gym.application.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<ID, T> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    T update(T entity);

    void deleteById(ID id);
}