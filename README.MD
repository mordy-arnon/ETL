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
    "keys" : ["_id"]
  }
}
```
