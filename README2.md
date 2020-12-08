# ETL
this project can help you move data (also BIG_DATA) from "source to target".

it is easy to run it (instractions: howToRun.md), as a local service, and configure it in files or in MongoDB.

for example:
this configuration:
```
{
  "_id":"/api/accounts/getByMCH?fieldName=balance",
  "description":"free text. the system does not read fields it does not know.",
  "source":{
    "class" : "SqlReader", 
    "driverClass" : "com.sqlserver.jdbc.Driver", 
    "connectionUrl" : "jdbc:sqlserver://innerServerBank:3306/LIVE", 
    "query" : "select +params.fieldName+ from accounts where MCH=+params.MCH+"
  },
  "target" : {
    "class" : "ApiWriter"
  }
}
```
this way we define a task that run in the schedule cron timing, with 4 threads (default in async), that read data from MySql with the above parameters, concatenate the field: "customerId" to _id and write it to mongoDB in upsert way.
it also create indices from given fields on the new collection.

this is only a small example to the posibilities.
want to see more? soon in "/examples".
