package backendjava.dto;

public class TransactionResponseWithWarning {
    private TransactionResponse transaction;
    private String warning;

    public TransactionResponseWithWarning(TransactionResponse transaction, String warning) {
        this.transaction = transaction;
        this.warning = warning;
    }

    public TransactionResponse getTransaction() {
        return transaction;
    }

    public String getWarning() {
        return warning;
    }

    public void setTransaction(TransactionResponse transaction) {
        this.transaction = transaction;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
