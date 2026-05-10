package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.User;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<User> userQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws Exception {
        userRepository = new UserRepository();

        Field emField = UserRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(userRepository, em);
    }

    @Test
    void saveShouldPersistAndReturnUser() {
        User user = createUser();

        User saved = userRepository.save(user);

        verify(em).persist(user);
    }

    @Test
    void findByIdShouldReturnUserWhenExists() {
        User user = createUser();

        when(em.find(User.class, 1L)).thenReturn(user);

        Optional<User> result = userRepository.findById(1L);

        assertTrue(result.isPresent());
        assertSame(user, result.get());
        verify(em).find(User.class, 1L);
    }

    @Test
    void findByIdShouldReturnEmptyWhenUserDoesNotExist() {
        when(em.find(User.class, 99L)).thenReturn(null);

        Optional<User> result = userRepository.findById(99L);

        assertTrue(result.isEmpty());
        verify(em).find(User.class, 99L);
    }

    @Test
    void findByUsernameShouldReturnUserWhenExists() {
        User user = createUser();

        when(em.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter("username", "John.Smith")).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of(user));

        Optional<User> result = userRepository.findByUsername("John.Smith");

        assertTrue(result.isPresent());
        assertSame(user, result.get());

        verify(em).createQuery(anyString(), eq(User.class));
        verify(userQuery).setParameter("username", "John.Smith");
        verify(userQuery).getResultList();
    }

    @Test
    void findByUsernameShouldReturnEmptyWhenUserDoesNotExist() {
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter("username", "Unknown.User")).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of());

        Optional<User> result = userRepository.findByUsername("Unknown.User");

        assertTrue(result.isEmpty());

        verify(em).createQuery(anyString(), eq(User.class));
        verify(userQuery).setParameter("username", "Unknown.User");
        verify(userQuery).getResultList();
    }

    @Test
    void findAllShouldReturnAllUsers() {
        User user1 = createUser();

        User user2 = User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Brown")
                .username("Mike.Brown")
                .password("password34")
                .isActive(true)
                .build();

        when(em.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of(user1, user2));

        List<User> result = userRepository.findAll();

        assertEquals(2, result.size());
        assertSame(user1, result.get(0));
        assertSame(user2, result.get(1));

        verify(em).createQuery(anyString(), eq(User.class));
        verify(userQuery).getResultList();
    }

    @Test
    void updateShouldMergeAndReturnUpdatedUserWhenUserExists() {
        User user = createUser();

        User mergedUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .username("Updated.Username")
                .password("password12")
                .isActive(false)
                .build();

        mockExistsById(1L, true);

        when(em.merge(user)).thenReturn(mergedUser);

        User result = userRepository.update(user);

        assertSame(mergedUser, result);
        assertEquals("Updated.Username", result.getUsername());
        assertFalse(result.getIsActive());

        verify(em).merge(user);
    }

    @Test
    void updateShouldThrowExceptionWhenUserIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userRepository.update(null)
        );

        assertEquals("User and user id must not be null", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenUserIdIsNull() {
        User user = createUser();
        user.setId(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userRepository.update(user)
        );

        assertEquals("User and user id must not be null", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenUserDoesNotExist() {
        User user = createUser();
        user.setId(99L);

        mockExistsById(99L, false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userRepository.update(user)
        );

        assertEquals("User not found. id=99", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void deleteByIdShouldRemoveUserWhenUserExists() {
        User user = createUser();

        when(em.find(User.class, 1L)).thenReturn(user);

        userRepository.deleteById(1L);

        verify(em).find(User.class, 1L);
        verify(em).remove(user);
    }

    @Test
    void deleteByIdShouldNotThrowExceptionWhenUserDoesNotExist() {
        when(em.find(User.class, 99L)).thenReturn(null);

        doThrow(new IllegalArgumentException())
                .when(em)
                .remove(isNull());

        assertDoesNotThrow(() -> userRepository.deleteById(99L));

        verify(em).find(User.class, 99L);
        verify(em).remove(null);
    }

    @Test
    void existsByIdShouldReturnTrueWhenUserExists() {
        mockExistsById(1L, true);

        boolean result = userRepository.existsById(1L);

        assertTrue(result);

        verify(countQuery).setParameter("id", 1L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void existsByIdShouldReturnFalseWhenUserDoesNotExist() {
        mockExistsById(99L, false);

        boolean result = userRepository.existsById(99L);

        assertFalse(result);

        verify(countQuery).setParameter("id", 99L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void existsByUsernameShouldReturnTrueWhenUserExists() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", "John.Smith")).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        boolean result = userRepository.existsByUsername("John.Smith");

        assertTrue(result);

        verify(em).createQuery(anyString(), eq(Long.class));
        verify(countQuery).setParameter("username", "John.Smith");
        verify(countQuery).getSingleResult();
    }

    @Test
    void existsByUsernameShouldReturnFalseWhenUserDoesNotExist() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", "Unknown.User")).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        boolean result = userRepository.existsByUsername("Unknown.User");

        assertFalse(result);

        verify(em).createQuery(anyString(), eq(Long.class));
        verify(countQuery).setParameter("username", "Unknown.User");
        verify(countQuery).getSingleResult();
    }

    private void mockExistsById(Long id, boolean exists) {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", id)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(exists ? 1L : 0L);
    }

    public User createUser(){
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .username("John.Smith")
                .password("password12")
                .isActive(true)
                .build();
    }
}