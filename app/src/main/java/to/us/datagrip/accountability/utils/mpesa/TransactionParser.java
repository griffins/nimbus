package to.us.datagrip.accountability.utils.mpesa;

import java.text.ParseException;

public class TransactionParser {
    private static final int TRANSACTION_LENGTH = 10;
    private String text;

    private TransactionParser(String text) {
        this.text = text;
    }

    private Transaction parse() throws ParseException {
        Transaction transaction = new Transaction();
        TextParser parser = TextParser.from(text);
        boolean amountSet = false;
        String prevToken = "";
        String prevToken2 = "";
        String account1 = null, account2 = null, dump = "";
        boolean capture = false;
        try {
            transaction.setReferenceNo(parseReferenceNo(parser.getTokens().get(0)));
        } catch (Exception ignored) {

        }
        for (String token : parser.getTokens()) {
            if (capture) {
                if (strCmp(token.toLowerCase(), "on") || strCmp(token.toLowerCase(), "for") || strCmp(token.toLowerCase(), "new")) {
                    capture = false;
                    if (account1 == null) {
                        account1 = dump.trim();
                    } else {
                        account2 = dump.trim();
                    }
                    dump = "";
                } else {
                    if (!strCmp("-", token)) {
                        dump += String.format("%s ", token);
                    }
                }
            } else {
                if ((strCmp(prevToken.toLowerCase(), "sent") && strCmp(token.toLowerCase(), "to")) ||
                        (strCmp(prevToken.toLowerCase(), "for") && strCmp(token.toLowerCase(), "account")) ||
                        (strCmp(prevToken.toLowerCase(), "cash") && strCmp(token.toLowerCase(), "to")) ||
                        (strCmp(prevToken.toLowerCase(), "paid") && strCmp(token.toLowerCase(), "to")) ||
                        (strCmp(prevToken.toLowerCase(), "airtime") && strCmp(token.toLowerCase(), "for")) ||
                        (strCmp(prevToken.toLowerCase(), "transferred") && strCmp(token.toLowerCase(), "to")) ||
                        strCmp(token.toLowerCase(), "from")) {
                    capture = true;
                }
            }

            try {
                transaction.setTransactionDate(DateParser.from(token));
            } catch (Exception ignored) {
            }

            try {
                double amount = parseAmount(prevToken, token);
                if (!amountSet) {
                    amountSet = true;
                    transaction.setAmount(amount);
                } else {
                    transaction.setBalance(amount);
                    // i think  balance is the last section of an mpesa transaction.
                    // Rest is just advert so just stop parsing;
                    break;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                transaction.setType(parseTransactionType(prevToken, token, prevToken2));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            prevToken2 = prevToken;
            prevToken = token;
        }

        transaction.setOtherAccountNo(account2);
        transaction.setOtherName(account1);
        if (transaction.getOtherAccountNo() == null &&
                transaction.getType() != Transaction.Types.TRANSFER_FROM &&
                transaction.getType() != Transaction.Types.TRANSFER_TO &&
                transaction.getType() != Transaction.Types.DEPOSIT &&
                transaction.getType() != Transaction.Types.BUYGOODS) {
            if (account1 != null) {
                String segs[] = account1.split(" ");
                if (transaction.getType() != Transaction.Types.WITHDRAWAL) {
                    transaction.setOtherAccountNo(segs[segs.length - 1]);
                } else {
                    transaction.setOtherAccountNo(segs[0]);
                }
                transaction.setOtherName(account1.replace(transaction.getOtherAccountNo(), "").trim());
            }
        }
        if (transaction.getBalance() == -1 && transaction.getAmount() >= 0) {
            transaction.setBalance(transaction.getAmount());
            transaction.setAmount(-1);
        }

        if (transaction.getBalance() >= 0 && transaction.getType() == -1) {
            transaction.setType(Transaction.Types.BALANCE_INQUIRY);
        }

        if (transaction.isInvalid()) {
            throw new ParseException("Invalid transaction", 0);
        }

        transaction.setText(getText());
        return transaction;
    }

    private String parseReferenceNo(String s) throws ParseException {
        if (s.length() >= TRANSACTION_LENGTH) {
            String ref = s.substring(0, 10);
            if (ref.contentEquals(ref.toUpperCase())) {
                return ref;
            }
        }
        throw new ParseException("Invalid Reference no", 0);
    }

    private int parseTransactionType(String prevToken, String token, String prevToken2) throws Exception {
        prevToken = prevToken.toLowerCase();
        prevToken2 = prevToken2.toLowerCase();
        token = token.toLowerCase();
        // 1 sent to $ - Sent
        // 1 sent to $ for account- Pay Bill
        // 1 paid to  -Buy Goods
        // 1 have received -Received
        // 1 have sent -Sent
        // 1 you bought  $ of airtime
        // 1 transferred to ->
        // 1 transferred from ->
        // 1 transaction $ has been reversed ->
        // 1 {pm}withdraw $ from
        //

        if (strCmp(prevToken, "for") && strCmp(token, "account")) {
            return Transaction.Types.PAYBILL;
        }

        if (strCmp(prevToken, "transferred") && strCmp(token, "to")) {
            return Transaction.Types.TRANSFER_TO;
        }
        if (strCmp(prevToken, "from") && strCmp(token, "your")) {
            return Transaction.Types.TRANSFER_FROM;
        }

        if (strCmp(prevToken, "sent") && strCmp(token, "to")) {
            return Transaction.Types.SENT;
        }
        if (strCmp(prevToken, "cash") && strCmp(token, "to")) {
            return Transaction.Types.DEPOSIT;
        }
        if (strCmp(prevToken, "have") && strCmp(token, "received")) {
            return Transaction.Types.RECEIVED;
        }
        if (strCmp(prevToken, "paid") && strCmp(token, "to")) {
            return Transaction.Types.BUYGOODS;
        }
        if (strCmp(prevToken, "airtime") && strCmp(token, "on")) {
            return Transaction.Types.AIRTIME_PUCHASE_SELF;
        }
        if (strCmp(prevToken, "airtime") && strCmp(token, "for")) {
            return Transaction.Types.AIRTIME_PUCHASE_OTHER;
        }
        if (strCmp(token, "reversed") && strCmp(prevToken, "been") && strCmp(prevToken2, "has")) {
            return Transaction.Types.TRANSACTION_REVERSAL;
        }
        if (strCmp(token, "amwithdraw") || strCmp(token, "pmwithdraw") || strCmp(token, "withdraw")) {
            return Transaction.Types.WITHDRAWAL;
        }

        throw new ParseException("Unable to parse transaction type", 0);
    }

    private boolean strCmp(String a, String b) {
        return a.contentEquals(b);
    }

    private double parseAmount(String... tokens) throws Exception {
        if (tokens.length > 1) {
            if (isCurrencyCode(tokens[0])) {
                String amount = tokens[1].replace(",", "");
                return Double.parseDouble(amount);
            }
        }
        throw new ParseException("Can't parse amount", 0);
    }

    private boolean isCurrencyCode(String token) {
        String codes[] = {"KES", "KSH", "SH"};
        for (String code : codes) {
            if (code.contentEquals(token.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public String getText() {
        return text;
    }

    public static Transaction from(String text) throws ParseException {
        return new TransactionParser(text).parse();
    }
}