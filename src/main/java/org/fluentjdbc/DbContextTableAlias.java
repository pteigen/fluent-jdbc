package org.fluentjdbc;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Used to specify a table in a {@link DbContextJoinedSelectBuilder} joined <code>SELECT</code>
 * statement. The same table can be joined several times by using different aliases. Example:
 *
 * <pre>
 * {@link DbContextTableAlias} perm = permissions.alias("perm");
 * DbContextTableAlias m = memberships.alias("m");
 * DbContextTableAlias p = persons.alias("p");
 * DbContextTableAlias g = persons.alias("granter");
 *
 * perm.join(perm.column("membership_id"), m.column("id"))
 *         .join(m.column("person_id"), p.column("id"))
 *         .join(perm.column("granted_by"), g.column("id"))
 *         .list(....);
 * </pre>
 */
public class DbContextTableAlias {
    private final DbContextTable table;
    private final DatabaseTableAlias alias;
    private final DatabaseTableReporter reporter;

    public DbContextTableAlias(DbContextTable table, String alias, DatabaseTableReporter reporter) {
        this.table = table;
        this.alias = table.getTable().alias(alias);
        this.reporter = reporter;
    }

    /**
     * Create a new {@link DatabaseColumnReference} object using this table, alias and the specified
     * column name
     */
    public DatabaseColumnReference column(String columnName) {
        return alias.column(columnName);
    }

    /**
     * Create a new {@link DbContextJoinedSelectBuilder} based on this table by joining the
     * specified {@link DatabaseColumnReference}
     * 
     * @see DbContextJoinedSelectBuilder#join(DatabaseColumnReference, DatabaseColumnReference)
     */
    public DbContextJoinedSelectBuilder join(DatabaseColumnReference a, DatabaseColumnReference b) {
        return select().join(a, b);
    }

    /**
     * Create a new {@link DbContextJoinedSelectBuilder} based on this table by joining the
     * specified joinedTable on all fields
     *
     * @see DbContextJoinedSelectBuilder#join(List, DbContextTableAlias, List)
     */
    public DbContextJoinedSelectBuilder join(List<String> leftFields, DbContextTableAlias joinedTable, List<String> rightFields) {
        return select().join(leftFields, joinedTable, rightFields);
    }

    /**
     * Create a new {@link DbContextJoinedSelectBuilder} based on this table by left joining the
     * specified {@link DatabaseColumnReference}.
     * 
     * @see DbContextJoinedSelectBuilder#leftJoin(DatabaseColumnReference, DatabaseColumnReference) 
     */
    public DbContextJoinedSelectBuilder leftJoin(DatabaseColumnReference a, DatabaseColumnReference b) {
        return select().leftJoin(a, b);
    }

    /**
     * Create a new {@link DbContextJoinedSelectBuilder} based on this table by left joining the
     * specified joinedTable on all fields
     *
     * @see DbContextJoinedSelectBuilder#join(List, DbContextTableAlias, List)
     */
    public DbContextJoinedSelectBuilder leftJoin(List<String> leftFields, DbContextTableAlias joinedTable, List<String> rightFields) {
        return select().leftJoin(leftFields, joinedTable, rightFields);
    }

    public DatabaseTableAlias getTableAlias() {
        return alias;
    }

    /**
     * Create a new {@link DbContextJoinedSelectBuilder} with the specified query
     *
     * <p>NOTE: Maybe {@link DbContextTableAlias} ought to implement all of {@link DatabaseQueryable}</p>
     */
    public DbContextJoinedSelectBuilder where(String fieldName, @Nullable Object value) {
        return select().whereExpression(alias.getAlias() + "." + fieldName + " = ?", value);
    }

    /**
     * Create a new {@link DbContextJoinedSelectBuilder}
     */
    public DbContextJoinedSelectBuilder select() {
        return new DbContextJoinedSelectBuilder(this);
    }

    public DbContext getDbContext() {
        return table.getDbContext();
    }

    public DatabaseTableReporter getReporter() {
        return reporter;
    }
}
