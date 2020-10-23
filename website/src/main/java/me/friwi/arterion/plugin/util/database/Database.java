package me.friwi.arterion.plugin.util.database;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.util.TriConsumer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Database {
    private boolean inTransaction = false;
    private Session session;
    private Transaction transaction;
    private long databaseThreadId = -1;

    public boolean beginTransaction() {
        if (databaseThreadId == -1) {
            databaseThreadId = Thread.currentThread().getId();
        }
        if (databaseThreadId != Thread.currentThread().getId()) {
            throw new RuntimeException("Please execute database calls from the database scheduler!");
        }
        if (inTransaction) return false;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            // start a transaction
            transaction = session.beginTransaction();
        } catch (Exception e) {
            printErrorAndCloseTransaction(e);
            return false;
        }
        inTransaction = true;
        return true;
    }

    public boolean commit() {
        if (!inTransaction) return false;
        try {
            // commit transaction
            session.flush();
            transaction.commit();
            session.close();
        } catch (Exception e) {
            printErrorAndCloseTransaction(e);
            return false;
        }
        inTransaction = false;
        session = null;
        transaction = null;
        return true;
    }

    public boolean rollback() {
        if (!inTransaction) return false;
        try {
            // commit transaction
            transaction.rollback();
            session.close();
        } catch (Exception e) {
            printErrorAndCloseTransaction(e);
            return false;
        }
        inTransaction = false;
        session = null;
        transaction = null;
        return true;
    }

    public void save(DatabaseEntity entity) {
        if (!inTransaction)
            beginTransaction();
        session.saveOrUpdate(entity);
    }

    public <T extends DatabaseEntity> T find(Class<? extends T> cl, Object id) {
        if (!inTransaction)
            beginTransaction();
        return session.find(cl, id);
    }

    public <T extends DatabaseEntity> List<T> findAllByCriteria(Class<T> cl, BiConsumer<CriteriaQuery, CriteriaBuilder> query) {
        if (!inTransaction) beginTransaction();
        // Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Create CriteriaQuery
        CriteriaQuery<T> criteria = builder.createQuery(cl);
        query.accept(criteria, builder);
        return session.createQuery(criteria).list();
    }

    public <T extends DatabaseEntity> long countAllByCriteria(Class<T> cl, TriConsumer<CriteriaQuery, CriteriaBuilder, Root<T>> query) {
        if (!inTransaction)
            beginTransaction();
        // Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Create CriteriaQuery
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<T> root = criteria.from(cl);
        criteria.select(builder.count(root));
        query.accept(criteria, builder, root);
        return session.createQuery(criteria).getSingleResult();
    }

    public <T extends DatabaseEntity> long countAllByColumnStart(Class<T> cl, @NotNull String column, @NotNull String value) {
        return countAllByCriteria(cl, (query, builder, root) -> query.where(builder.like(builder.upper(root.get(column)), value.toUpperCase() + "%")));
    }

    public <T extends DatabaseEntity> long countAllContaining(Class<T> cl, @NotNull String column[], @NotNull Object contains) {
        return countAllByCriteria(cl, (query, builder, root) -> {
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.isMember(contains, root.get(column[i]));
            }
            query.where(builder.or(predicates));
        });
    }

    public <T extends DatabaseEntity> long countAllConjunction(Class<T> cl, @NotNull String column[], @NotNull Object[] match) {
        return countAllByCriteria(cl, (query, builder, root) -> {
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.equal(root.get(column[i]), match[i]);
            }
            query.where(builder.or(predicates));
        });
    }

    public <T extends DatabaseEntity> long countAllContainingWithNotNull(Class<T> cl, @NotNull String column[], @NotNull Object contains, String notNullColumn) {
        return countAllByCriteria(cl, (query, builder, root) -> {
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.isMember(contains, root.get(column[i]));
            }
            query.where(builder.or(predicates), builder.isNotNull(root.get(notNullColumn)));
        });
    }

    public <T extends DatabaseEntity> List<T> findAllContainingWithSortAndLimit(Class<T> cl, @NotNull String[] column, @NotNull Object contains, String sortColumn, boolean asc, int begin, int limit) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.isMember(contains, root.get(column[i]));
            }
            query.where(builder.or(predicates));
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> List<T> findAllConjunctionWithSortAndLimit(Class<T> cl, @NotNull String[] column, @NotNull Object[] match, String sortColumn, boolean asc, int begin, int limit) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.equal(root.get(column[i]), match[i]);
            }
            query.where(builder.or(predicates));
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> List<T> findAllContainingWithMatch(Class<T> cl, @NotNull String[] column, @NotNull Object contains, String matchColumn, Object match) {
        return findAllByCriteria(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.isMember(contains, root.get(column[i]));
            }
            query.where(builder.or(predicates), builder.equal(root.get(matchColumn), match));
        });
    }

    public <T extends DatabaseEntity> List<T> findAllContainingWithNotNullWithSortAndLimit(Class<T> cl, @NotNull String[] column, @NotNull Object contains, String notNullColumn, String sortColumn, boolean asc, int begin, int limit) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.isMember(contains, root.get(column[i]));
            }
            query.where(builder.or(predicates), builder.isNotNull(root.get(notNullColumn)));
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> List<T> findAllByColumnStartWithSortAndLimit(Class<T> cl, @NotNull String column, @NotNull String value, String sortColumn, boolean asc, int begin, int limit) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            query.where(builder.like(builder.upper(root.get(column)), value.toUpperCase() + "%"));
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> long countAllWithMatches(Class<T> cl, @NotNull String[] column, @NotNull Object[] value) {
        return countAllByCriteria(cl, (query, builder, root) -> {
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.equal(root.get(column[i]), value[i]);
            }
            query.where(predicates);
        });
    }

    public <T extends DatabaseEntity> List<T> findAllWithMatchesWithSortAndLimit(Class<T> cl, @NotNull String[] column, @NotNull Object[] value, String sortColumn, boolean asc, int begin, int limit) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            Predicate[] predicates = new Predicate[column.length];
            for (int i = 0; i < column.length; i++) {
                predicates[i] = builder.equal(root.get(column[i]), value[i]);
            }
            query.where(predicates);
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> long countAll(Class<T> cl) {
        return countAllByCriteria(cl, (query, builder, root) -> query.where());
    }

    public <T extends DatabaseEntity> List<T> findAllWithSortAndLimit(Class<T> cl, String sortColumn, boolean asc, int begin, int limit) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> long countAllWithNotNull(Class<T> cl, String notNullColumn) {
        return countAllByCriteria(cl, (query, builder, root) -> query.where(builder.isNotNull(root.get(notNullColumn))));
    }

    public <T extends DatabaseEntity> List<T> findAllWithSortAndLimitWithNotNull(Class<T> cl, String sortColumn, boolean asc, int begin, int limit, String notNullColumn) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            query.where(builder.isNotNull(root.get(notNullColumn)));
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> long countAllByColumnStartAndMatch(Class<T> cl, @NotNull String column, @NotNull String value, @NotNull String columnMatch, Object match) {
        return countAllByCriteria(cl, (query, builder, root) -> {
            query.where(builder.like(builder.upper(root.get(column)), value.toUpperCase() + "%"));
            query.where(builder.equal(root.get(columnMatch), match));
        });
    }

    public <T extends DatabaseEntity> List<T> findAllByColumnStartWithSortAndLimitAndMatch(Class<T> cl, @NotNull String column, @NotNull String value, String sortColumn, boolean asc, int begin, int limit, @NotNull String columnMatch, Object match) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> root = query.from(cl);
            query.where(builder.like(builder.upper(root.get(column)), value.toUpperCase() + "%"));
            query.where(builder.equal(root.get(columnMatch), match));
            if (asc) query.orderBy(ImmutableList.of(builder.asc(root.get(sortColumn))));
            else query.orderBy(ImmutableList.of(builder.desc(root.get(sortColumn))));
        }, begin, limit);
    }

    public <T extends DatabaseEntity> List<T> findAllByCriteriaWithLimit(Class<T> cl, BiConsumer<CriteriaQuery, CriteriaBuilder> query, int limit) {
        return findAllByCriteriaWithLimit(cl, query, 0, limit);
    }

    public <T extends DatabaseEntity> List<T> findAllByCriteriaWithLimit(Class<T> cl, BiConsumer<CriteriaQuery, CriteriaBuilder> query, int begin, int limit) {
        if (!inTransaction)
            beginTransaction();
        // Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Create CriteriaQuery
        CriteriaQuery<T> criteria = builder.createQuery(cl);
        query.accept(criteria, builder);
        return session.createQuery(criteria).setFirstResult(begin).setMaxResults(limit).list();
    }

    public <T extends DatabaseEntity> Stream<T> findAllByCriteriaStream(Class<T> cl, BiConsumer<CriteriaQuery<T>, CriteriaBuilder> query) {
        if (!inTransaction)
            beginTransaction();
        // Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Create CriteriaQuery
        CriteriaQuery<T> criteria = builder.createQuery(cl);
        query.accept(criteria, builder);
        return session.createQuery(criteria).stream();
    }

    public <T extends DatabaseEntity> List<T> findAll(Class<T> cl) {
        return findAllByCriteria(cl, (query, builder) -> query.select(query.from(cl)));
    }

    public <T extends DatabaseEntity> List<T> findAllByColumn(Class<T> cl, @NotNull String column, @NotNull Object value) {
        return findAllByCriteria(cl, (query, builder) -> query.where(builder.equal(query.from(cl).get(column), value)));
    }

    public <T extends DatabaseEntity> List<T> findAllSortByColumnDescWithLimit(Class<T> cl, @NotNull String column, int limit) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> from = query.from(cl);
            query.select(from).orderBy(ImmutableList.of(builder.desc(from.get(column))));
        }, limit);
    }

    public <T extends DatabaseEntity> List<T> findAllSortByColumnDescWithLimitWithColumnMatch(Class<T> cl, @NotNull String column, int limit, @NotNull String columnMatch, Object match) {
        return findAllByCriteriaWithLimit(cl, (query, builder) -> {
            Root<T> from = query.from(cl);
            query.select(from).where(builder.equal(from.get(columnMatch), match)).orderBy(builder.desc(from.get(column)));
        }, limit);
    }

    public <T extends DatabaseEntity> List<T> findAllByColumnIgnoreCase(Class<T> cl, @NotNull String column, @NotNull String value) {
        return findAllByCriteria(cl, (query, builder) -> {
            Root<T> from = query.from(cl);
            query.select(from).where(builder.equal(builder.upper(from.get(column)), value.toUpperCase()));
        });
    }

    public <T extends DatabaseEntity> List<T> findAllByColumn(Class<T> cl, String columns[], Object values[]) {
        return findAllByCriteria(cl, (query, builder) -> {
            Root<T> from = query.from(cl);
            Predicate[] predicates = new Predicate[columns.length];
            for (int i = 0; i < columns.length; i++) {
                if (values[i] == null) {
                    predicates[i] = builder.isNull(from.get(columns[i]));
                } else {
                    predicates[i] = builder.equal(from.get(columns[i]), values[i]);
                }
            }
            query.select(from).where(predicates);
        });
    }

    /**
     * Finds all entities by exact columns and columns by range
     *
     * @param cl            Entity type
     * @param columnsEqual  Columns to match equally
     * @param values        to be equal
     * @param columnsRanged Columns to be matched by range
     * @param valuesLow     Lower value, inclusive
     * @param valuesHigh    Higher value, exclusive
     * @param <T>           Entity type
     * @return
     */
    public <T extends DatabaseEntity> List<T> findAllByColumnAndRange(Class<T> cl, String columnsEqual[], Object values[], String columnsRanged[], Comparable valuesLow[], Comparable valuesHigh[]) {
        return findAllByCriteria(cl, (query, builder) -> {
            Root<T> from = query.from(cl);
            Predicate[] predicates = new Predicate[columnsEqual.length + columnsRanged.length * 2];
            for (int i = 0; i < columnsEqual.length; i++) {
                predicates[i] = builder.equal(from.get(columnsEqual[i]), values[i]);
            }
            for (int i = 0; i < columnsRanged.length; i++) {
                predicates[columnsEqual.length + i * 2] = builder.greaterThanOrEqualTo(from.get(columnsRanged[i]), valuesLow[i]);
                predicates[columnsEqual.length + i * 2 + 1] = builder.lessThan(from.get(columnsRanged[i]), valuesHigh[i]);
            }
            query.where(predicates);
        });
    }

    public <T extends DatabaseEntity> Stream<T> findAllNonNullByColumnStream(Class<T> cl, String column) {
        return findAllByCriteriaStream(cl, (query, builder) -> query.where(builder.isNotNull(query.from(cl).get(column))));
    }

    public <T extends DatabaseEntity> T findOneByCriteria(Class<T> cl, BiConsumer<CriteriaQuery, CriteriaBuilder> query) {
        if (!inTransaction)
            beginTransaction();
        // Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Create CriteriaQuery
        CriteriaQuery<T> criteria = builder.createQuery(cl);
        query.accept(criteria, builder);
        try {
            List<T> res = session.createQuery(criteria).getResultList();
            if (res.size() <= 0) return null;
            else return res.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T extends DatabaseEntity> T findOneByColumn(Class<T> cl, String column, Object value) {
        return findOneByCriteria(cl, (query, builder) -> query.where(builder.equal(query.from(cl).get(column), value)));
    }

    public void delete(DatabaseEntity entity) {
        if (!inTransaction)
            beginTransaction();
        session.delete(entity);
    }

    private void printErrorAndCloseTransaction(Exception e) {
        e.printStackTrace();
        if (transaction != null) {
            transaction.rollback();
        }
        if (session != null) {
            session.close();
        }
        session = null;
        transaction = null;
        inTransaction = false;
    }
}
