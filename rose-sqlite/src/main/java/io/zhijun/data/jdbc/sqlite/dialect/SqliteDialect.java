package io.zhijun.data.jdbc.sqlite.dialect;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.data.relational.core.dialect.AbstractDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.IdGeneration;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.LockOptions;

/**
 * SQL {@link Dialect} for SQLite.
 */
public class SqliteDialect extends AbstractDialect {

    protected SqliteDialect() {
    }

    private static final IdentifierProcessing IDENTIFIER_PROCESSING =
            IdentifierProcessing.create(IdentifierProcessing.Quoting.ANSI, IdentifierProcessing.LetterCasing.AS_IS);

    private static final LimitClause LIMIT_CLAUSE = new LimitClause() {
        @Override
        public String getLimit(long limit) {
            return String.format("LIMIT %d", limit);
        }

        @Override
        public String getOffset(long offset) {
            return String.format("OFFSET %d", offset);
        }

        @Override
        public String getLimitOffset(long limit, long offset) {
            return String.format("LIMIT %d OFFSET %d", limit, offset);
        }

        @Override
        public Position getClausePosition() {
            return Position.AFTER_ORDER_BY;
        }
    };

    /**
     * SQLite uses file-level locking, so no SQL lock clause is emitted.
     */
    private static final LockClause LOCK_CLAUSE = new LockClause() {
        @Override
        public String getLock(LockOptions lockOptions) {
            return "";
        }

        @Override
        public Position getClausePosition() {
            return Position.AFTER_ORDER_BY;
        }
    };

    /**
     * SQLite manages identity internally via {@code ROWID} and does not annotation sequences.
     * Batch ID generation is disabled because the SQLite JDBC driver does not return
     * generated keys from batch statement executions.
     */
    private static final IdGeneration ID_GENERATION = new IdGeneration() {
        @Override
        public boolean driverRequiresKeyColumnNames() {
            return false;
        }

        @Override
        public boolean supportedForBatchOperations() {
            return false;
        }
    };

    @Override
    public LimitClause limit() {
        return LIMIT_CLAUSE;
    }

    @Override
    public LockClause lock() {
        return LOCK_CLAUSE;
    }

    @Override
    public IdentifierProcessing getIdentifierProcessing() {
        return IDENTIFIER_PROCESSING;
    }

    @Override
    public IdGeneration getIdGeneration() {
        return ID_GENERATION;
    }

    @Override
    public Collection<Object> getConverters() {
        return Arrays.asList(
                NumberToBooleanConverter.INSTANCE,
                TimestampAtUtcToOffsetDateTimeConverter.INSTANCE
        );
    }
}
