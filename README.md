# Blockchain Ledger — Java OOP Final Project

A console-based **simplified cryptocurrency ledger**. It loads wallets and pending
transactions from CSV files, validates them, mines blocks with proof-of-work,
keeps wallet balances up to date and detects tampering with the chain.

This is an educational project: it focuses on object-oriented design, business
logic and testing, not on a real distributed blockchain. There is no networking
and there are no real cryptographic signatures.

---

## Requirements
- JDK 25
- Maven 3.9+

## How to run it
From the project root:

```bash
mvn compile exec:java
```

Or in IntelliJ IDEA: open `Application.java` and click **Run**.

## How to run the tests
```bash
mvn test
```

Or in IntelliJ IDEA: right-click `src/test/java` → **Run 'All Tests'**.

---

## Main features
_TODO: list the features once they work, for example:_
- _Load wallets and pending transactions from CSV files_
- _Validate transactions (sufficient funds, positive amount, no double spending)_
- _Mine blocks with proof-of-work; the miner receives the reward plus fees_
- _Show wallet balances and the chain_
- _Validate the chain and detect tampering_

## Main classes
| Class | Package | Responsibility |
|---|---|---|
| `Wallet` | `model` | Identity of a participant (address, owner name) |
| _TODO_ | | |

## Project structure
```
src/
├── main/
│   ├── java/com/nali/blockchain/
│   │   ├── model/       domain objects (Wallet, Transaction, Block, ...)
│   │   ├── service/     business logic (Ledger, Mempool, mining, validation)
│   │   ├── io/          loading data from files
│   │   ├── exception/   custom exceptions
│   │   ├── ui/          console interaction
│   │   └── Application.java
│   └── resources/data/  CSV files loaded at startup
└── test/
    ├── java/com/nali/blockchain/   JUnit tests
    └── resources/                  test data files
```

---

## Technical decisions
_TODO: 2 to 5 decisions, each with the reason. Candidates:_
1. _**`BigDecimal` for money** — why not `double`?_
2. _**The chain is the only source of truth for balances** — why balances are not stored in `Wallet`_
3. _**Blocks and transactions cannot change once created** — why_
4. _**Mining as a swappable strategy (interface)** — what this gives you in tests_
5. _**Skip invalid CSV lines instead of crashing** — and when the app does stop_

## Inheritance
_TODO: the `Transaction` hierarchy, and the custom exception hierarchy._

## Polymorphism
_TODO: where a list of `Transaction` objects is processed through the parent type, and what differs per subclass._

## Abstract class / interface
_TODO: which ones, and **why** an abstract class in one place and an interface in another._

## Custom exceptions
| Exception | Thrown when |
|---|---|
| `BlockchainException` | Base class for all business-rule violations (checked) |
| `InsufficientFundsException` | A wallet tries to spend more than it has available |
| _TODO_ | |

## How the application loads its data
_TODO: describe the files once the loaders exist:_
- _`src/main/resources/data/wallets.csv` — columns `address,owner,initialBalance`_
- _`src/main/resources/data/transactions.csv` — columns `from,to,amount,fee`_
- _What happens with a missing file, and what happens with a malformed line._

## SOLID principles
_TODO: one concrete example from your code for each principle:_
- _**S** — Single Responsibility:_
- _**O** — Open/Closed:_
- _**L** — Liskov Substitution:_
- _**I** — Interface Segregation:_
- _**D** — Dependency Inversion:_