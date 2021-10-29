```mermaid
flowchart TD
    gov-model --> ddd-shared;
    
    calendar-model --> ddd-shared;
    
    money-model --> ddd-shared;
    
    banking-model --> ddd-shared;
    banking-model --> gov-model;
    banking-model --> money-model;
    
    loan-model --> ddd-shared;
    loan-model --> money-model;
    loan-model --> calendar-model;
    
    cashreceipt-model --> ddd-shared;
    cashreceipt-model --> gov-model;
    cashreceipt-model --> money-model;

    accounting-model --> ddd-shared;
    accounting-model --> gov-model;
    accounting-model --> money-model;
    accounting-model --> loan-model;
    accounting-model --> banking-model;
    accounting-model --> calendar-model;
    accounting-model --> cashreceipt-model;
```
