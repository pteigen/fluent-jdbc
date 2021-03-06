package org.fluentjdbc;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Provides a starting point for for fluent-jdbc with explicit Connection management.</p>
 *
 * <p>Example</p>
 * <pre>
 * DatabaseTable table = new DatabaseTableImpl("database_table_test_table");
 * Object id = table.insert()
 *     .setPrimaryKey("id", null)
 *     .setField("code", 1002)
 *     .setField("name", "insertTest")
 *     .execute(connection);
 *
 * List&lt;Long&gt; result = table.where("name", "insertTest").orderBy("code").listLongs(connection, "code");
 * table.where("id", id).setField("name", updatedName).execute(connection);
 * table.where("id", id).delete(connection);
 * </pre>
 */
public class DatabaseTableImpl implements DatabaseTable {

    private final String tableName;
    private final DatabaseTableReporter reporter;

    public DatabaseTableImpl(String tableName) {
        this(tableName, DatabaseTableReporter.LOGGING_REPORTER);
    }

    public DatabaseTableImpl(String tableName, DatabaseTableReporter reporter) {
        this.tableName = tableName;
        this.reporter = reporter;
    }

    /**
     * If you haven't called {@link #orderBy}, the results of {@link DatabaseListableQueryBuilder#list}
     * will be unpredictable. Call <code>unordered()</code> if you are okay with this.
     */
    @Override
    public DatabaseListableQueryBuilder unordered() {
        return new DatabaseTableQueryBuilder(this, reporter);
    }

    /**
     * Adds an <code>order by</code> clause to the query. Needed in order to list results
     * in a predictable order.
     */
    @Override
    public DatabaseListableQueryBuilder orderBy(String orderByClause) {
        return query().orderBy(orderByClause);
    }

    @Override
    public DatabaseTableAlias alias(String alias) {
        return new DatabaseTableAlias(tableName, alias, reporter);
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    /**
     * Creates a {@link DatabaseSaveBuilder} which creates a <code>INSERT</code> or <code>UPDATE</code>
     * statement, depending on whether the row already exists in the database. If idValue is null,
     * {@link DatabaseSaveBuilder} will attempt to use the table's autogeneration of primary keys
     * if there is no row with matching unique keys
     */
    @Override
    public DatabaseSaveBuilder<Long> newSaveBuilder(String idField, @Nullable Long id) {
        return new DatabaseSaveBuilderWithLong(this, idField, id);
    }

    /**
     * Creates a {@link DatabaseSaveBuilder} which creates a <code>INSERT</code> or <code>UPDATE</code>
     * statement, depending on whether the row already exists in the database. Throws exception if idValue is null
     */
    @Override
    public DatabaseSaveBuilder<String> newSaveBuilderWithString(String idField, String id) {
        return new DatabaseSaveBuilderWithoutGeneratedKeys<>(this, idField, id);
    }

    /**
     * Use instead of {@link #newSaveBuilder} if the database driver does not
     * support RETURN_GENERATED_KEYS
     */
    @Override
    public DatabaseSaveBuilder<Long> newSaveBuilderNoGeneratedKeys(String idField, @Nullable Long id) {
        return new DatabaseSaveBuilderWithoutGeneratedKeys<>(this, idField, id);
    }

    /**
     * Creates a {@link DatabaseSaveBuilder} which creates a <code>INSERT</code> or <code>UPDATE</code>
     * statement, depending on whether the row already exists in the database.
     * Generates UUID.randomUUID if idValue is null and row with matching unique keys does not already exist
     */
    @Override
    public DatabaseSaveBuilderWithUUID newSaveBuilderWithUUID(String idField, @Nullable UUID id) {
        return new DatabaseSaveBuilderWithUUID(this, idField, id);
    }

    @Override
    public DatabaseSimpleQueryBuilder query() {
        return new DatabaseTableQueryBuilder(this, reporter);
    }

    /**
     * Creates a {@link DatabaseInsertBuilder} object to fluently generate a <code>INSERT ...</code> statement
     */
    @Override
    public DatabaseInsertBuilder insert() {
        return new DatabaseInsertBuilder(tableName, reporter.operation("INSERT"));
    }

    /**
     * Creates a {@link DatabaseBulkInsertBuilder} object to fluently generate a <code>INSERT ...</code> statement
     * for a list of objects. Example:
     *
     * <pre>
     *     public void saveAll(List&lt;TagType&gt; tagTypes, Connection connection) {
     *         tagTypesTable.bulkInsert(tagTypes)
     *             .setField("name", TagType::getName)
     *             .generatePrimaryKeys("id", TagType::setId)
     *             .execute(connection);
     *     }
     * </pre>
     */
    @Override
    public <T> DatabaseBulkInsertBuilder<T> bulkInsert(Iterable<T> objects) {
        return new DatabaseBulkInsertBuilder<>(this, objects);
    }

    /**
     * Creates a {@link DatabaseBulkInsertBuilder} object to fluently generate a <code>INSERT ...</code> statement
     * for a list of objects. Example:
     *
     * <pre>
     *     public void saveAll(Stream&lt;TagType&gt; tagTypes, Connection connection) {
     *         tagTypesTable.bulkInsert(tagTypes)
     *             .setField("name", TagType::getName)
     *             .generatePrimaryKeys("id", TagType::setId)
     *             .execute(connection);
     *     }
     * </pre>
     */
    @Override
    public <T> DatabaseBulkInsertBuilder<T> bulkInsert(Stream<T> objects) {
        return bulkInsert(objects.collect(Collectors.toList()));
    }

    /**
     * Creates a {@link DatabaseBulkDeleteBuilder} object to fluently generate a <code>DELETE ...</code> statement
     * for a list of objects. Example:
     *
     * <p>Example:</p>
     *
     * <pre>
     *     public void deleteAll(List&lt;TagType&gt; tagTypes, Connection connection) {
     *         tagTypesTable.bulkDelete(tagTypes)
     *              .where("id", TagType::getId)
     *              .execute(connection);
     *     }
     * </pre>
     */
    @Override
    public <T> DatabaseBulkDeleteBuilder<T> bulkDelete(Iterable<T> objects) {
        return new DatabaseBulkDeleteBuilder<>(this, objects);
    }

    @Override
    public <T> DatabaseBulkUpdateBuilder<T> bulkUpdate(Iterable<T> objects) {
        return new DatabaseBulkUpdateBuilder<>(this, objects);
    }

    /**
     * Creates a {@link DatabaseUpdateBuilder} object to fluently generate a <code>UPDATE ...</code> statement
     */
    @Override
    public DatabaseUpdateBuilder update() {
        return new DatabaseUpdateBuilder(tableName, reporter.operation("UPDATE"));
    }

    /**
     * Executes <code>DELETE FROM tableName WHERE ....</code>
     */
    @Override
    public DatabaseDeleteBuilder delete() {
        return new DatabaseDeleteBuilder(tableName, reporter.operation("DELETE"));
    }
}
