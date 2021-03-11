package core;

import org.slf4j.Logger;
import play.api.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @param <T1> Type of the item
 * @param <T2> Type of the item ID
 */
public abstract class BaseRepository<T1 extends Single<T2>, T2> {

    public BaseRepository(final Database db, final Logger logger) {
        this.database = db;
        this.logger = logger;
    }

    final Database database;

    final Logger logger;

    // Get all
    public List<T1> getAll(final String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildGetAllRequest(transaction, organization);

                final ResultSet result = ps.executeQuery();

                final List<T1> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get item from database", e);
                return new ArrayList<>();
            }
        });
    }

    // Get specific number of rows
    public List<T1> getPage(final String organization, final Integer offset, final Integer length) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildGetPageRequest(transaction, organization, offset, length);

                final ResultSet result = ps.executeQuery();

                final List<T1> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get item from database", e);
                return new ArrayList<>();
            }
        });
    }

    protected abstract PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException;

    protected abstract PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException;

    protected abstract T1 parseOneItem(ResultSet result, String organization) throws SQLException;

    // Get one

    public Optional<T1> get(final String organization, final T2 id) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildGetRequest(transaction, organization, id);

                final ResultSet result = ps.executeQuery();

                if (result.next()) {
                    return Optional.of(parseOneItem(result, organization));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Failed to get item from database", e);
                return Optional.empty();
            }
        });
    }

    protected abstract PreparedStatement buildGetRequest(final Connection transaction, final String organization, final T2 id) throws SQLException;

    // Search

    public List<T1> search(final String organization, final String pattern) {
        final List<String> tokens = Arrays.asList(pattern.trim().split(" "));
        final StringJoiner joiner = new StringJoiner(":* & ", "", ":*");
        tokens.forEach(token -> joiner.add(token));

        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildSearchRequest(transaction, organization, joiner.toString());

                final ResultSet result = ps.executeQuery();

                final List<T1> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get item from database", e);
                return Collections.emptyList();
            }
        });
    }

    protected abstract PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException;

    public List<T1> searchPage(final String organization, final String pattern, final Integer offset, final Integer length) {
        final List<String> tokens = Arrays.asList(pattern.trim().split(" "));
        final StringJoiner joiner = new StringJoiner(":* & ", "", ":*");
        tokens.forEach(token -> joiner.add(token));

        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildSearchPageRequest(transaction, organization, joiner.toString(), offset, length);

                final ResultSet result = ps.executeQuery();

                final List<T1> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get item from database", e);
                return Collections.emptyList();
            }
        });
    }

    protected abstract PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException;

    // Add

    public Optional<T2> add(final String organization, final T1 item) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildAddRequest(transaction, organization, item);
                ps.execute();
                return Optional.of(item.getId());
            } catch (SQLException e) {
                this.logger.error("Failed to insert item " + item.getId() + " into database", e);
                return Optional.empty();
            }
        });
    }

    protected abstract PreparedStatement buildAddRequest(final Connection transaction, final String organization, final T1 item) throws SQLException;

    // Delete
    public Optional<T2> delete(final String organization, final T2 id) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildDeleteRequest(transaction, organization, id);
                ps.execute();
                return Optional.of(id);
            } catch (SQLException e) {
                this.logger.error("Failed to delete item " + id + " into database", e);
                return Optional.empty();
            }
        });
    }

    protected abstract PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final T2 id) throws SQLException;

    // Update
    public Optional<T1> update(final String organization, final T1 itemUpdate) {
        Boolean updated = database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = buildUpdateRequest(transaction, organization, itemUpdate);
                ps.execute();
                return true;
            } catch (SQLException e) {
                this.logger.error("Failed to update item " + itemUpdate.getId().toString() + " into database", e);
                return false;
            }
        });
        if (updated) {
            return get(organization, itemUpdate.getId());
        } else {
            return Optional.empty();
        }
    }

    public Optional<T1> safeUpdate(final String organization, final T1 itemUpdate) {
        final Optional<T1> existingItem = get(organization, itemUpdate.getId());

        if (existingItem.isPresent()) {
            return update(organization, itemUpdate);
        } else {
            return Optional.empty();
        }
    }

    protected abstract PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final T1 item) throws SQLException;

    // Upsert
    public T1 upsert(String organization, final T1 item) {
        final Optional<T1> existingItem = get(organization, item.getId());

        if (existingItem.isPresent()) {
            update(organization, item);
            return item;
        } else {
            add(organization, item);
            return item;
        }
    }
}
