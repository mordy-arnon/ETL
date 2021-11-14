```
{
  "_id":"moveCustomerDataFromSqlToMongo", 
  "description":"free text. the system does not read fields it does not know.",
  "schedule" : "*/5 8-18 * * *",  
  "async" : 4, 
  "source":{
    "class" : "SqlReader",
    "driverClass" : "com.mysql.jdbc.Driver", 
    "connectionUrl" : "jdbc:mysql://codect.cvoaszdfghq.us-west-2.rds.amazonaws.com:3306/codect", 
    "query" : "select * from customer_data c where c.active=true", 
    "batchSize" : NumberInt(1000)
  }, 
  "transforms" : [
    {  "class" : "StringFromFieldsTrans",
       "to" : "_id",
       "fields" : ["customerId"]
     },
     {
       "class":"AddFieldTrans",
       "fieldName":"update_at",
       "value":"->datetime"
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
