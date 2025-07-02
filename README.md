# E-commerce Order System

**Description**

This is a simple system for managing orders in an online store.

---

## Features

- **Register Customers**  
  Customers can register with their contact and address details.

- **Add Products**  
  Products have a name, price, and VAT tax.

- **Place Orders**  
  Customers can place orders with one or more products. Each order has customer info, product list with quantity, and calculates total net and gross prices.

- **Get Order Details**  
  Get order information by order ID. It returns customer data, products with quantities and prices, and order totals.

---

## Technologies Used

- Spring Boot (Spring Web and Spring Data JPA)
- Relational database (H2)
- REST API
- JUnit, Mockito

---

## Database configuration 
This project uses an embedded H2 database to store data. The configuration is set in the application properties:
```js
spring.application.name=ecommerce-app

spring.datasource.url=jdbc:h2:file:./data/ecommerce;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```
## Endpoints
### 1. Registration (POST)
#### Path : 
```js
http://localhost:8080/api/auth/register
```
#### Body (JSON)
```js
        {
          "firstName": "testname",
          "lastName": "testlastname ",
          "username": "testusername",
          "email": "mail@test.pl",
          "password": "Testuserpassword123",
          "phoneNumber": "111222333",
          "country": "testcountry",
          "city": "testcity",
          "street": "testcity",
          "postalCode": "00-111"
        }
```
#### Response (JSON)
```js 
        {   
          "id": 193,
          "username": "testusername",
          "email": "mail@test.pl",
          "message": "Registration successful",
          "errors": null
        }
```
### 2. Login (POST)
#### Path :
```js
http://localhost:8080/api/auth/login
```
#### Body (JSON)
```js
        {
          "username": "testusername",
          "password": "Testuserpassword123"
        }
```
#### Response (JSON)
The token in the response is used for authorization.
You need to include this token in the Authorization header when you call other API endpoints.
```js 
        {
          "token": "35e044f2-fc45-45d5-822f-f07766ce51b2",
          "message": "Login successful"
        }
```
### 3. Logout (DELETE)
#### Path :
```js
http://localhost:8080/api/auth/logout
 ```
To log out you need to use the token in the authorization header
#### Response (JSON)

```js 
        
Logout successful
        
```
### 4. Add product (POST)
#### Path :
```js
http://localhost:8080/api/products/add
```
#### Body (JSON)
The product can only be added by a user with the admin role
```js

        {
          "name": "Book",
          "price": 10,
          "vat": 23.00,
          "quantity" : "100"
        }
```
#### Response (JSON)
```js 
        {
          "id": 97,
          "name": "Book",
          "price": 10,
          "vat": 23.00,
          "quantity": 100,
          "message": "Product added successfully"
        }
```
### 5. Place order (POST)
#### Path :
```js
http://localhost:8080/api/orders/place-order
```
#### Body (JSON)
```js
SINGLE ORDER
        {
          "items": [
            {
              "productId": 65,
              "quantity": 2
            }
          ]
        }
        
MULTIPLE ORDER

        {
          "items": [
          {
            "productId": 65,
            "quantity": 2
          },
          {
            "productId": 66,
            "quantity": 3
          },
          {
            "productId": 67,
            "quantity": 1
          }
        ]
        }
```
#### Response (JSON)
```js 
        {
          "orderId": 33,
                  "message": "Order placed successfully",
                  "totalNet": 20.00,
                  "totalGross": 24.60,
                  "productSummaries": [
            "book x2"
          ]
        }
```
### 6. Get order (POST)
#### Path :
```js
http://localhost:8080/api/orders/get/order_id
```
User with the user role can only get own orders and a user with the admin role can get all orders
#### Response (JSON)
```js 
            {
            "orderId": 1,
              "customer": {
                "id": 1,
                "username": "testusername",
                "email": "mail@test.pl",
                "firstName": "testname",
                "lastName": "testlastname",
                "phoneNumber": "+48111222333",
                "country": "testcountry",
                "city": "testcity",
                "street": "teststreet",
                "postalCode": "00-111"
            },
             "items": 
              [
              {
                "productId": 65,
                "productName": "book",
                "quantity": 2,
                "netPrice": 20.00,
                "grossPrice": 24.60
              }
              ],
              "totalNet": 20.00,
              "totalGross": 24.60,
              "message": null
            }
```
