# ETL
this project can help you move data (also BIG_DATA) from "source to target".

it is easy to run it (instractions: howToRun.md), as a local service, and configure it in files or in MongoDB.

for example:
this configuration:
```
{
  "_id":"moveCustomerDataFromSqlToMongo",
  "description":"free text. the system does not read fields it does not know.",
  "schedule" : "*/5 8-18 * * *", 
  "async" : true, 
  "source":{
    "class" : "SqlReader", 
    "driverClass" : "com.mysql.jdbc.Driver", 
    "connectionUrl" : "jdbc:mysql://codect.cvoaszdfghq.us-west-2.rds.amazonaws.com:3306/codect", 
    "query" : "select * from customer_data e", 
    "batchSize" : NumberInt(1000)
  }, 
  "transforms" : [
    {  "class" : "StringFromFieldsTrans",
       "to" : "_id",
       "fields" : ["customerId"]
     }
  ],
  "target" : {
    "class" : "ReplaceOneMongoWriter", 
    "writeToCollection" : "customer", 
    "upsert" : "insert if not exist", 
    "keys" : ["eventType","localEventNum"], 
    "indices" : [["eventType","localEventNum"],["lastModified"]]
  }
}
```
this way we define a task that run in the schedule cron timing, with 4 threads (default in async), that read data from MySql with the above parameters, concatenate the field: "customerId" to _id and write it to mongoDB in upsert way.
it also create indices from given fields on the new collection.

this is only a small example to the posibilities.
want to see more? soon in "/examples".
