package org.fluentjdbc;

import java.util.Collection;

public class DbContextUpdateBuilder implements DatabaseUpdateable<DbContextUpdateBuilder> {

    private DbTableContext tableContext;
    private DatabaseUpdateBuilder updateBuilder;

    public DbContextUpdateBuilder(DbTableContext tableContext, DatabaseUpdateBuilder updateBuilder) {
        this.tableContext = tableContext;
        this.updateBuilder = updateBuilder;
    }

    @Override
    public DbContextUpdateBuilder setFields(Collection<String> fields, Collection<?> values) {
        updateBuilder.setFields(fields, values);
        return this;
    }

    @Override
    public DbContextUpdateBuilder setField(String field, Object value) {
        updateBuilder.setField(field, value);
        return this;
    }

    public int execute() {
        return updateBuilder.execute(tableContext.getConnection());
    }


}
