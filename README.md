# Book store application.
Spring REST API Service.

## How to install:
1. Clone project from GitHub
1. Import libraries with gradle
1. Install postgreSQL 9+
1. Create new user:  
   ```
   Username: user  
   Password: user
   ```
  
1. Create two empty databases with this names:  
```spring-example```  
```spring-example-test```

1. Run with ```"%project_path%\gradlew bootRun"``` command  
Api will be able at: ```http://localhost:8080/```
1. *(Optional)* Use ```POST /api/books/fill``` and ```POST /api/users/fill``` to populate database

## Api commands
### Books Api:
```POST /api/books``` - create a new book from request body *[example bellow]*  
```POST /api/books/fill``` - fill books table with ten dummy books  
```GET /api/books``` - get all books  
```GET /api/books/1``` - get book by id  
```DELETE /api/books/5``` - delete book by id  

### Users Api:
```POST /api/users/fill``` - fill users table with three dummy users  
```GET /api/users``` - get all users  
```GET /api/users/11``` - get user by id  
```GET /api/users/11/orders``` - get all **orders** by user id

### Orders Api:
```POST /api/orders``` - create a new order from request body *[example bellow]*  
```POST /api/orders/14/pay``` - set status of order *(by id)* as PAID  
```GET /api/orders``` - get all orders  
```GET /api/orders/14``` - get order by id  
```GET /api/orders/filter?userId=12``` - get orders by user id  
```DELETE /api/orders/14``` - delete order by id 

## POST request examples

### POST /api/books:
```$xslt
{
    "name" : "Book name",
    "price" : 555.25,
    "quantity" : 10
}
```

### POST /api/orders:
```$xslt
{
   "books":[
      {
         "bookId":1,
         "quantity":2
      },
      {
         "bookId":2,
         "quantity":3
      }
   ],
   "userId":12
}
```
