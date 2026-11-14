# Blockchain Ledger — Java OOP Final Project

## Purpose
A console application that simulates a **simplified cryptocurrency ledger**.
It loads wallets and pending transactions from CSV files, checks them against business rules,
puts them into blocks using proof-of-work mining, keeps every wallet balance up to date
and detects if someone tampered with the chain.

It is an educational project about object-oriented design, business logic and testing.
It is **not** a real distributed blockchain: there is no network and no cryptographic signature.

---

## Requirements
- JDK 21 or later
- Maven is **not** required: the project contains the Maven wrapper (`mvnw`), which downloads
  the right Maven version the first time it runs (internet access needed once).

## How to run it
From the project root, on Linux or macOS:

```bash
./mvnw compile exec:java
```

On Windows:

```bash
mvnw.cmd compile exec:java
```

If Maven is installed, `mvn compile exec:java` works too.
Or in IntelliJ IDEA: open `Application.java` and click **Run**.

## How to run the tests
```bash
./mvnw test
```

(`mvnw.cmd test` on Windows, or `mvn test`.)

Or in IntelliJ IDEA: right-click `src/test/java` → **Run 'All Tests'**.

---

## Main features
- Load wallets with their initial balance and pending transactions from CSV files
- Validate transactions: positive amount, non-negative fee, different sender and receiver,
  known wallets, enough money
- Prevent **double spending**: money already promised in pending transactions cannot be spent again
- Mine blocks with **proof-of-work**; the transactions with the highest fees are chosen first
  and the miner receives a reward of 50 plus all the fees of the block
- Show the wallets ranked by balance, the pending transactions and the whole chain
- Validate the whole chain: block order, links, proof-of-work, **miner rewards** and balances
- Show how tampering with a block is detected
- Add a block **all or nothing**: if one of its transactions fails, the balances, the chain and
  the pending transactions stay unchanged

## Main classes
| Class | Package | Responsibility |
|---|---|---|
| `Wallet` | `model` | Identity of a participant (address, owner name) |
| `Ledger` | `model` | Current balance of every address; balances can never become negative |
| `Transaction` | `model` | Abstract parent of all transactions |
| `TransferTransaction` | `model` | Money sent from one wallet to another, with a fee |
| `RewardTransaction` | `model` | New money: mining reward and initial balances |
| `Block` | `model` | Immutable group of transactions with its SHA-256 hash |
| `Blockchain` | `model` | Ordered list of blocks |
| `Mempool` | `service` | Pending transactions, double-spending check, choice by fee |
| `MiningStrategy` / `ProofOfWork` | `service` | How blocks are mined and checked |
| `ConsensusRules` | `service` | The miner reward rule, shared by mining and validation |
| `ChainValidator` | `service` | Checks the whole chain |
| `BlockchainService` | `service` | Main business operations used by the UI |
| `DataLoader` / `CsvDataLoader` | `io` | Loading the initial data |
| `ConsoleMenu` | `ui` | User interaction only |
| `HashUtil` | `util` | SHA-256 helper |
| `MoneyFormatter` | `util` | Shows every amount the same way (`54` instead of `54.0`) |
| `Application` | root | Creates the objects and starts the menu |

## Project structure
```
src/
├── main/
│   ├── java/com/nali/blockchain/
│   │   ├── model/       domain objects
│   │   ├── service/     business logic
│   │   ├── io/          loading data from files
│   │   ├── exception/   custom exceptions
│   │   ├── ui/          console interaction (and its input exception)
│   │   ├── util/        helpers
│   │   └── Application.java
│   └── resources/data/  CSV files loaded at startup
└── test/
    ├── java/com/nali/blockchain/   JUnit tests
    └── resources/test-data/        CSV files used by the tests
```

---

## Technical decisions
1. **`BigDecimal` for money.** `double` cannot store values like 0.1 exactly, so sums of amounts
   and fees would slowly become wrong. Amounts are compared with `compareTo()`, because
   `equals()` treats `1.0` and `1.00` as different.
2. **Balances come from the chain, not from `Wallet`.** The initial balances are put as
   `RewardTransaction`s in the first (genesis) block. The `Ledger` is only changed by applying
   blocks. So the whole chain can be replayed from the beginning to check that nobody spent
   money they did not have.
3. **Blocks cannot be changed after creation.** All fields are `final` and the list of
   transactions is copied with `List.copyOf()`. Changing a block means creating a new one,
   which has a different hash, so tampering is detected.
4. **Mining is an interface (`MiningStrategy`).** `BlockchainService` and `ChainValidator`
   only know the interface. The application uses `ProofOfWork` with difficulty 4,
   the tests use difficulty 1 so they run fast, and another algorithm could be added
   without changing those classes.
5. **An invalid CSV line is skipped, not fatal.** The line is ignored and a warning is shown,
   so one typo does not stop the application. The application only stops when a file is
   missing or contains no valid wallet.

## Inheritance
- `Transaction` (abstract) → `TransferTransaction` and `RewardTransaction`.
  Both are transactions with an id, a receiver, an amount and a timestamp, but they change
  balances in a different way.
- Custom exceptions: `BlockchainException` → `InsufficientFundsException`,
  `InvalidTransactionException`, `InvalidChainException`, `DataLoadException`.
  Code can catch one specific error or all business errors at once.

## Polymorphism
A block holds a `List<Transaction>`. `BlockchainService.applyAndAdd()` and `ChainValidator`
call `transaction.applyTo(ledger)` on every element without knowing its real type:
- a `TransferTransaction` takes amount + fee from the sender and gives the amount to the receiver;
- a `RewardTransaction` only gives money to the receiver.

`validateSpecificRules()`, `getSender()`, `getFee()` and `getType()` are also overridden.
`ProofOfWork` is used through the `MiningStrategy` type, and `CsvDataLoader` through the
`DataLoader` type.

## Abstract class / interface
- **Abstract class `Transaction`:** every transaction shares the same fields and common code
  (`validate()`, `toDataString()`), and only `applyTo()`, `getSender()` and `getType()`
  differ. An abstract class can hold shared fields and code; an interface cannot hold fields.
  `validate()` is `final`: it always checks the common rule (positive amount) and then calls
  `validateSpecificRules()`, which a subclass can override to add its own rules
  (`TransferTransaction` checks the fee and the receiver). A subclass can add rules but cannot
  skip the common ones.
- **Interface `MiningStrategy`:** it only describes a capability ("mine a block", "check a hash")
  with no shared data, and it allows different implementations to be swapped.
- **Interface `DataLoader`:** the application does not depend on the file format; a JSON
  loader could be added without changing `Application`.

## Custom exceptions
| Exception | Thrown when |
|---|---|
| `BlockchainException` | Base class of all business errors (checked) |
| `InsufficientFundsException` | A wallet tries to spend more than it has available |
| `InvalidTransactionException` | Non-positive amount, negative fee, sending to yourself, unknown wallet, transaction submitted twice |
| `InvalidChainException` | Validation finds a wrong index, a broken link, an invalid proof-of-work, a wrong miner reward or an invalid transaction |
| `DataLoadException` | A data file is missing, unreadable or contains no valid wallet |
| `InvalidInputException` (package `ui`) | The user types something the menu cannot use (not a number, a block that does not exist) or the input ends. It does not extend `BlockchainException` because it is not a business rule. |

The console menu catches only `BlockchainException` and `InvalidInputException`; other exceptions
are programming errors and are not hidden behind an "invalid input" message.

Standard exceptions are also used: `IllegalArgumentException` (bad arguments, invalid CSV line),
`NumberFormatException` (invalid number), `NullPointerException` (via `Objects.requireNonNull`),
`IllegalStateException`, `IOException` and `NoSuchAlgorithmException`.

## How the application loads its data
At startup, `Application` uses `CsvDataLoader` to read two files from `src/main/resources/data/`
(they are on the classpath, so they are found from any working directory):
- `wallets.csv` — columns `address,owner,initialBalance`. Each line becomes a `Wallet`, and
  the initial balances are put into the genesis block.
- `transactions.csv` — columns `from,to,amount,fee`. Each line becomes a `TransferTransaction`
  that is submitted to the mempool.

The first line of each file is a header. A malformed line (wrong number of columns,
invalid number, negative balance, duplicate address) is skipped and a warning is printed.
A transaction that breaks a business rule is rejected with a message. The provided
`transactions.csv` contains two such transactions on purpose: Charlie has no money,
and Alice tries to spend money that she has already promised (double spending).

## SOLID principles
- **S — Single Responsibility:** each class has one job. `CsvDataLoader` only reads files,
  `Mempool` only manages pending transactions, `ChainValidator` only checks the chain,
  `ConsensusRules` only holds the reward rule, `ConsoleMenu` only talks to the user and
  `Application` only creates the objects and connects them.
- **O — Open/Closed:** a new mining algorithm is a new `MiningStrategy`, and a new file format
  is a new `DataLoader`; the classes that use them do not change. For transactions it is
  partly true: the code that applies and checks transactions (`BlockchainService.applyAndAdd`,
  `ChainValidator`) works with any `Transaction` subclass, but a new kind of transaction also
  needs a new value in the `TransactionType` enum. Users can only submit transfers on purpose:
  `Mempool`, `BlockchainService.submitTransaction()` and `DataLoader` use `TransferTransaction`,
  because rewards must only be created by mining.
- **L — Liskov Substitution:** any `Transaction` subclass can be used where a `Transaction`
  is expected. Because `validate()` is `final`, every subclass keeps the common rules, and
  `RewardTransaction.applyTo()` throws fewer exceptions than its parent, never more.
- **I — Interface Segregation:** `MiningStrategy` and `DataLoader` are small interfaces with
  only the methods their users need.
- **D — Dependency Inversion:** `BlockchainService` and `ChainValidator` depend on the
  `MiningStrategy` interface, not on `ProofOfWork`, and `Application` works with a
  `DataLoader`. In addition, `BlockchainService` receives all the objects it uses
  (`Ledger`, `Blockchain`, `Mempool`, `ChainValidator`) in its constructor, and `Application`
  creates them. This is why the tests can use an easier difficulty, and why a test can pass its
  own `Ledger` to check that a failing block leaves everything unchanged.
