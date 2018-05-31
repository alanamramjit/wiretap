public class Bank extends Thread {
  static final int MAGICP = 1883;

  public final Account [] accounts;
  public final Transaction [] transactions;
  private int total;

  public Bank (int na, int nt) {
    accounts = new Account [na];
    transactions = new Transaction [nt];
  }

  public int sum () {
    int sum = 0;
    for (Account a : accounts) {
      sum += a.amount;
    }
    return sum;
  }

  public void makeAccounts() {
    for (int i = 0; i < accounts.length; i++) {
      accounts[i] = new Account(100);
    }
    total = sum();
  }

  public void makeTransactions() {
    for (int i = 0; i < transactions.length; i++) {
      Account from = accounts[(i * MAGICP) % accounts.length];
      Account to = accounts[(i * MAGICP * 2) % accounts.length];
      transactions[i] = new Transaction(from, to, 10);
    }
  }

  public static void main (String [] args) throws InterruptedException {
    Bank bank = new Bank(4, 10000);

    bank.makeAccounts();
    bank.makeTransactions();

    bank.start();

    for (Transaction t : bank.transactions)
      t.start();

    for (Transaction t : bank.transactions)
      t.join();

    bank.interrupt();

  }

  public void run () {
    try {
      while (true) {
        assert(total == sum());
        Thread.sleep(1);
      }
    } catch (InterruptedException e) {}
  }

  private static class Account {
    public int amount;

    public Account(int amount) {
      this.amount = amount;
    }
  }

  private static class Transaction extends Thread {
    public final Account from;
    public final Account to;
    public final int amount;

    public Transaction(Account from, Account to, int amount) {
      this.from = from;
      this.to = to;
      this.amount = amount;
    }

    public void run() {
      from.amount -= amount;
      to.amount += amount;
    }

  }
}
