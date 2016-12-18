package com.stolser.javatraining.webproject.model.dao.invoice;

import com.stolser.javatraining.webproject.model.entity.invoice.Invoice;
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical;
import com.stolser.javatraining.webproject.model.entity.user.User;
import com.stolser.javatraining.webproject.model.storage.StorageException;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.*;

public class MysqlInvoiceDao implements InvoiceDao {
    private static final String EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE_ID =
            "Exception during execution statement '%s' for invoiceId = %d.";
    private static final String EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_USER_ID =
            "Exception during execution statement '%s' for userId = %d.";
    private static final String EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE =
            "Exception during execution statement '%s' for invoice = %s.";
    private static final String EXCEPTION_DURING_GETTING_INVOICE_SUM = "Exception during execution statement '%s' for since = %s " +
            "and until = '%s'.";
    private static final String EXCEPTION_DURING_EXECUTION_FOR_PERIODICAL_ID = "Exception during execution statement '%s' for periodicalId = %d.";
    private Connection conn;

    public MysqlInvoiceDao(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Invoice findOneById(long invoiceId) {
        String sqlStatement = "SELECT * FROM invoices WHERE id = ?";

        try {
            PreparedStatement st = conn.prepareStatement(sqlStatement);
            st.setLong(1, invoiceId);

            ResultSet rs = st.executeQuery();

            return rs.next() ? getInvoiceFromRs(rs) : null;

        } catch (SQLException e) {
            String message = String.format(EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE_ID,
                    sqlStatement, invoiceId);
            throw new StorageException(message, e);
        }
    }

    @Override
    public List<Invoice> findAllByUserId(long userId) {
        String sqlStatement = "SELECT * FROM invoices " +
                "JOIN users ON (invoices.user_id = users.id) " +
                "WHERE users.id = ?";

        try {
            return executeAndGetInvoicesFromRs(sqlStatement, userId);

        } catch (SQLException e) {
            String message = String.format(EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_USER_ID,
                    sqlStatement, userId);
            throw new StorageException(message, e);
        }
    }

    @Override
    public List<Invoice> findAllByPeriodicalId(long periodicalId) {
        String sqlStatement = "SELECT * FROM invoices " +
                "JOIN periodicals ON (invoices.periodical_id = periodicals.id) " +
                "WHERE periodicals.id = ?";

        try {
            return executeAndGetInvoicesFromRs(sqlStatement, periodicalId);

        } catch (SQLException e) {
            String message = String.format(EXCEPTION_DURING_EXECUTION_FOR_PERIODICAL_ID,
                    sqlStatement, periodicalId);
            throw new StorageException(message, e);
        }
    }

    private List<Invoice> executeAndGetInvoicesFromRs(String sqlStatement, long periodicalId)
            throws SQLException {
        PreparedStatement st = conn.prepareStatement(sqlStatement);
        st.setLong(1, periodicalId);

        ResultSet rs = st.executeQuery();

        List<Invoice> invoices = new ArrayList<>();

        while (rs.next()) {
            invoices.add(getInvoiceFromRs(rs));
        }

        return invoices;
    }

    @Override
    public long getCreatedInvoiceSumByCreationDate(Instant since, Instant until) {
        String sqlStatement = "SELECT SUM(total_sum) FROM invoices " +
                "WHERE creation_date >= ? AND creation_date <= ?";

        try {
            PreparedStatement st = conn.prepareStatement(sqlStatement);
            st.setTimestamp(1, new Timestamp(since.toEpochMilli()));
            st.setTimestamp(2, new Timestamp(until.toEpochMilli()));


            ResultSet rs = st.executeQuery();
            rs.next();

            return rs.getLong(1);

        } catch (SQLException e) {
            String message = String.format(EXCEPTION_DURING_GETTING_INVOICE_SUM,
                    sqlStatement, since, until);
            throw new StorageException(message, e);
        }
    }

    @Override
    public long getPaidInvoiceSumByPaymentDate(Instant since, Instant until) {
        String sqlStatement = "SELECT SUM(total_sum) FROM invoices " +
                "WHERE payment_date >= ? AND payment_date <= ? AND status = ?";

        try {
            PreparedStatement st = conn.prepareStatement(sqlStatement);
            st.setTimestamp(1, new Timestamp(since.toEpochMilli()));
            st.setTimestamp(2, new Timestamp(until.toEpochMilli()));
            st.setString(3, Invoice.Status.PAID.name().toLowerCase());

            ResultSet rs = st.executeQuery();
            rs.next();

            return rs.getLong(1);

        } catch (SQLException e) {
            String message = String.format(EXCEPTION_DURING_GETTING_INVOICE_SUM,
                    sqlStatement, since, until);
            throw new StorageException(message, e);
        }
    }


    @Override
    public void createNew(Invoice invoice) {
        String sqlStatement = "INSERT INTO invoices " +
                "(user_id, periodical_id, period, total_sum, creation_date, payment_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement st = conn.prepareStatement(sqlStatement);

            setCreateUpdateStatementFromInvoice(st, invoice);

            st.executeUpdate();

        } catch (SQLException e) {
            String message = String.format(EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE,
                    sqlStatement, invoice);
            throw new StorageException(message, e);
        }
    }

    @Override
    public int update(Invoice invoice) {
        String sqlStatement = "UPDATE invoices " +
                "SET user_id=?, periodical_id=?, period=?, total_sum=?, creation_date=?, " +
                "payment_date=?, status=? WHERE id=?";

        try {
            PreparedStatement st = conn.prepareStatement(sqlStatement);

            setCreateUpdateStatementFromInvoice(st, invoice);
            st.setLong(8, invoice.getId());

            return st.executeUpdate();

        } catch (SQLException e) {
            String message = String.format(EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE,
                    sqlStatement, invoice);
            throw new StorageException(message, e);
        }
    }

    private Invoice getInvoiceFromRs(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getLong(DB_INVOICES_ID));

        User user = new User();
        user.setId(rs.getLong(DB_INVOICES_USER_ID));
        invoice.setUser(user);

        Periodical periodical = new Periodical();
        periodical.setId(rs.getLong(DB_INVOICES_PERIODICAL_ID));
        invoice.setPeriodical(periodical);

        invoice.setSubscriptionPeriod(rs.getInt(DB_INVOICES_PERIOD));
        invoice.setTotalSum(rs.getLong(DB_INVOICES_TOTAL_SUM));
        invoice.setCreationDate(getCreationDateFromResults(rs));
        invoice.setPaymentDate(getPaymentDateFromResults(rs));
        invoice.setStatus(Invoice.Status.valueOf(rs.getString(DB_INVOICES_STATUS).toUpperCase()));

        return invoice;
    }

    private Instant getCreationDateFromResults(ResultSet rs) throws SQLException {
        return Instant.ofEpochMilli(rs.getTimestamp(DB_INVOICES_CREATION_DATE).getTime());
    }

    private Instant getPaymentDateFromResults(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(DB_INVOICES_PAYMENT_DATE);

        return (timestamp != null) ? Instant.ofEpochMilli(timestamp.getTime()) : null;
    }

    private void setCreateUpdateStatementFromInvoice(PreparedStatement st, Invoice invoice) throws SQLException {
        st.setLong(1, invoice.getUser().getId());
        st.setLong(2, invoice.getPeriodical().getId());
        st.setInt(3, invoice.getSubscriptionPeriod());
        st.setDouble(4, invoice.getTotalSum());
        st.setTimestamp(5, new Timestamp(invoice.getCreationDate().toEpochMilli()));
        st.setTimestamp(6, getPaymentDate(invoice));
        st.setString(7, invoice.getStatus().name().toLowerCase());
    }

    private Timestamp getPaymentDate(Invoice invoice) {
        Instant paymentDate = invoice.getPaymentDate();

        return (paymentDate != null) ? new Timestamp(paymentDate.toEpochMilli()) : null;
    }

    @Override
    public List<Invoice> findAll() {
        throw new UnsupportedOperationException();
    }
}
