package org.fluentjdbc;

import java.sql.SQLException;
import java.util.Collection;

import org.fluentjdbc.util.ExceptionUtil;

/**
 * Generate <code>INSERT</code> statements by collecting field names and parameters. Support
 * autogeneration of primary keys. Example:
 *
 * <pre>
 * {@link DbContextTable} table = context.table("database_test_table");
 * try (DbContextConnection ignored = context.startConnection(dataSource)) {
 *     Long id = table.insert()
 *      .setPrimaryKey("id", (Long)null)
 *      .setField("name", "Something")
 *      .setField("code", 102)
 *      .execute();
 * }
 * </pre>
 *
 * @see DatabaseInsertBuilder
 */
public class DbContextInsertBuilder implements DatabaseUpdatable<DbContextInsertBuilder> {

    public class DbContextInsertBuilderWithPk<T> {

        private final DatabaseInsertWithPkBuilder<T> builder2;

        public DbContextInsertBuilderWithPk(DatabaseInsertWithPkBuilder<T> builder) {
            builder2 = builder;
        }

        public DbContextInsertBuilderWithPk<T> setField(String fieldName, Object parameter) {
            builder2.setField(fieldName, parameter);
            return this;
        }

        public T execute() {
            try {
                return builder2.execute(dbContextTable.getConnection());
            } catch (SQLException e) {
                throw ExceptionUtil.softenCheckedException(e);
            }
        }
    }

    private final DatabaseInsertBuilder builder;
    private final DbContextTable dbContextTable;

    public DbContextInsertBuilder(DbContextTable dbContextTable) {
        this.dbContextTable = dbContextTable;
        builder = dbContextTable.getTable().insert();
    }

    /**
     * Adds primary key to the <code>INSERT</code> statement if idValue is not null. If idValue is null
     * this will {@link java.sql.PreparedStatement#execute(String, String[])} to generate the primary
     * key using the underlying table autogeneration mechanism
     */
    public <T> DbContextInsertBuilderWithPk<T> setPrimaryKey(String idField, T idValue) {
        DatabaseInsertWithPkBuilder<T> setPrimaryKey = builder.setPrimaryKey(idField, idValue);
        return new DbContextInsertBuilderWithPk<>(setPrimaryKey);
    }

    /**
     * Adds fieldName to the <code>INSERT (fieldName) VALUES (?)</code> and parameter to the list of parameters
     */
    public DbContextInsertBuilder setField(String fieldName, Object parameter) {
        builder.setField(fieldName, parameter);
        return this;
    }

    /**
     * Calls {@link #setField(String, Object)} for each fieldName and parameter
     */
    @Override
    public DbContextInsertBuilder setFields(Collection<String> fields, Collection<?> values) {
        builder.setFields(fields, values);
        return this;
    }

    /**
     * Executes the insert statement and returns the number of rows inserted
     */
    public int execute() {
        return builder.execute(dbContextTable.getConnection());
    }
}
