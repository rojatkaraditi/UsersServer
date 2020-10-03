const { response } = require("express");
const express = require("express");
const { request } = require("http");
const mongo = require('mongodb');
const jwt = require('jsonwebtoken');
const { requestBody, validationResult, body, header, param, query } = require('express-validator');
const user = require("./User");
const User = require("./User");
const NodeCache = require('node-cache');
const crypto = require('crypto')

const MongoClient = mongo.MongoClient;
const uri = "mongodb+srv://rojatkaraditi:AprApr_2606@test.z8ya6.mongodb.net/project4DB?retryWrites=true&w=majority";
const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true});
var collection;
const tokenSecret = "wFq9+ssDbT#e2H9^";
var decoded={};
var token;
const myCache = new NodeCache( { stdTTL: 3600, checkperiod: 60 } );


var connectToDb = function(req,res,next){
    client.connect(err => {
      if(err){
          return res.status(400).json({"error":"Could not connect to database: "+err});
      }
      collection = client.db("project4DB").collection("users");
      console.log("connected to database");
    next();
    });
}

var verifyToken = function(req,res,next){
    token = req.header("_xToken");
    if(!token){
        return res.status(400).json({"error":"_xToken needs to be provided for using API"})
    }
    if(myCache.has(token)){
        return res.status(400).json({"error":"Cannot proceed. User is logged out"})
    }
    try {
        decoded = jwt.verify(token, tokenSecret);
        next();
      } catch(err) {
        return res.status(400).json({"error":err});
      }
}


const route = express.Router();
route.use(connectToDb);
route.use("/users",verifyToken);

route.post("/signup",[
    body("firstName","firstName cannot be empty").notEmpty().trim().escape(),
    body("firstName","firstName can have only alphabets").isAlpha().trim().escape(),
    body("lastName","lastName cannot be empty").notEmpty().trim().escape(),
    body("lastName","lastName can have only alphabets").isAlpha().trim().escape(),
    body("gender","gender cannot be empty").notEmpty().trim().escape(),
    body("gender","gender can have only alphabets").isAlpha().trim().escape(),
    body("gender","gender can only be male or female").isIn(["male","female","Male","Female"]),
    body("age","age is needed to create user").notEmpty(),
    body("age","age has to be an integer value").isInt(),
    body("email","email cannot be empty").notEmpty().trim().escape(),
    body("email","invalid email format").isEmail(),
    body("password","password cannot be empty").notEmpty().trim()
],(request,response)=>{
    const err = validationResult(request);
    if(!err.isEmpty()){
        return response.status(400).json({"error":err});
    }
    try{
        let data = request.body.password;
    let buff = new Buffer(data, 'base64');
    let pwd = buff.toString('ascii');
    var hash = crypto.createHash('md5').update(pwd).digest('hex');
    var newUser = new User(request.body);
    newUser.password=hash;
    collection.insertOne(newUser,(err,res)=>{
        var result={};
        var responseCode = 200;
        if(err){
            result={"error":err};
            responseCode=400;
        }
        else{
            //console.log(res);

            if(res.ops.length>0){
                var usr = res.ops[0].getUser();
                usr.exp = Math.floor(Date.now() / 1000) + (60 * 60);
                var token = jwt.sign(usr, tokenSecret);
                result=res.ops[0].getUser();
                result.token=token;
            }
            
        }
        return response.status(responseCode).json(result);
    });
    }
    catch(error){
        return response.status(400).json({"error":error});
    }
}); 

route.get("/login",[
    header("_xData","_xData header required to login").notEmpty().trim()
],(request,response)=>{

    const err = validationResult(request);
    if(!err.isEmpty()){
        return response.status(400).json({"error":err});
    }
    
    try{
        var data = request.header('_xData');
    let buff = new Buffer(data, 'base64');
    let loginInfo = JSON.parse(buff.toString('ascii'));
    var result ={};

    if(loginInfo!=undefined && loginInfo!=null && loginInfo.email!=undefined && loginInfo.email!=null && loginInfo.password!=undefined && loginInfo.password!=null){
        var query = {"email":loginInfo.email};
        collection.find(query).toArray((err,res)=>{
            var responseCode = 400;
            if(err){
                result = {"error":err};
            }
            else if(res.length<=0){
                result={"error":"no such user present"};
            }
            else{
                var user = new User(res[0]);
                var hash = crypto.createHash('md5').update(loginInfo.password).digest('hex');
                if(user.password===hash){
                    result=user.getUser();
                    user=user.getUser();
                    user.exp = Math.floor(Date.now() / 1000) + (60 * 60);
                    var token = jwt.sign(user, tokenSecret);
                    result.token=token;
                    responseCode=200;
                }
                else{
                    result={"error":"Username or password is incorrect"};
                }
            }

            return response.status(responseCode).json(result);

        });
    }
    else{
        return response.status(400).json({"error":"credentials not provided for login"});
    }
    }
    catch(error){
        return response.status(400).json({"error":error.toString()});
    }

});

route.get("/users/logout",(request,response)=>{
    const expiryTime = ((decoded.exp*1000)-Date.now())/1000;
    
    if(expiryTime>0){
         var result = myCache.set(token,decoded.exp,expiryTime);
         if(!result){
             return response.status(400).json({"error":"could not logout user"});
         }
         return response.status(200).json({"result":"user logged out"});
    }
    else{
        return response.status(400).json({"error":"token expired"});
    }
});


route.get("/users/:id",[
    param("id","id needs to be specified for fetching user").exists(),
    param("id","id should be a mongodb id").isMongoId()
],(request,response)=>{
    try{
        var err = validationResult(request);
        if(!err.isEmpty()){
            return response.status(400).json({"error":err});
        }
        var query = {"_id":new mongo.ObjectID(request.params.id)};
        var result={};
        var responseCode = 400;
        collection.find(query,{ projection: { password: 0 } }).toArray((err,res)=>{
            if(err){
                result = {"error":err};
            }
            else{
                if(res.length<=0){
                    result={"error":"no user found with id "+request.params.id};
                }
                else{
                    result = res[0];
                    responseCode=200;
                }
            }
            return response.status(responseCode).json(result);
        });
    }
    catch(error){
        return response.status(400).json({"error":error.toString()});
    }
});

route.get("/users",[
    query("firstName","firstName should only have alphabets").optional().isAlpha().trim().escape(),
    query("lastName","lastName should only have alphabets").optional().isAlpha().trim().escape(),
    query("gender","gender can only be male or female").optional().isIn(["male","female","Male","Female"]),
    query("email","invalid email format").optional().isEmail()
],(request,response)=>{
    var err =validationResult(request);
    if(!err.isEmpty()){
        return response.status(400).json({"error":err});
    }
    try{
        var query = {};
    if(request.query.firstName){
        var rule = {"$regex": ".*"+request.query.firstName+"*.", "$options": "i"}
        query.firstName=rule;
    }
    if(request.query.lastName){
        var rule = {"$regex": ".*"+request.query.lastName+"*.", "$options": "i"}
        query.lastName=rule;
    }
    if(request.query.gender){
        query.gender=request.query.gender;
    }
    if(request.query.email){
        query.email=request.query.email;
    }

    var result = {};
    var users = [];
    var responseCode = 400;
    collection.find(query,{ projection: { password: 0 } }).toArray((err,res)=>{
        if(err){
            result={"error":err};
        }
        else{
            if(res.length<=0){
                result={"error":"no users found"};
            }
            else{
                res.forEach(usr=>{
                    users.push(new User(usr));
                });
                result={"users":users};
                responseCode=200;
            }
        }
        return response.status(responseCode).json(result);
    });

    }
    catch(error){
        return response.status(400).json({"error":error.toString()});
    }
});


route.put("/users",[
    body("_id","id needs to be specified for update").notEmpty().trim(),
    body("_id","invalid id").isMongoId(),
    body("firstName","firstName can have only alphabets").optional().isAlpha().trim().escape(),
    body("lastName","lastName can have only alphabets").optional().isAlpha().trim().escape(),
    body("gender","gender can only be male or female").optional().isIn(["male","female","Male","Female"]),
    body("age","age has to be an integer value").optional().isInt(),
    body("email","email cannot be updated").isEmpty()
],(request,response)=>{
    var err = validationResult(request);
    if(!err.isEmpty()){
        return response.status(400).json({"error":err});
    }
    try{
        var updateData={};
        if(request.body.firstName){
            updateData.firstName=request.body.firstName;
        }
        if(request.body.lastName){
            updateData.lastName=request.body.lastName;
        }
        if(request.body.gender){
            updateData.gender=request.body.gender;
        }
        if(request.body.age){
            updateData.age=request.body.age;
        }
        if(updateData.firstName || updateData.lastName || updateData.gender || updateData.age){
            var query={"_id":mongo.ObjectID(request.body._id)};

            collection.find(query,{ projection: { _id: 1 } }).toArray((err,res)=>{
                if(err){
                    return response.status(400).json({"error":err});
                }
                if(res.length<=0){
                    return response.status(400).json({"error":"no user found with id "+request.body._id});
                }

                var newQuery = {$set : updateData};

                var result={};
                var responseCode=400;
                collection.updateOne(query,newQuery,(err,res)=>{
                    if(err){
                        result={"error":err};
                    }
                    else{
                            result = {"result":"user updated"};
                            responseCode=200;
                    }

                    return response.status(responseCode).json(result);
                });
            });
            
        }
        else{
            return response.status(200).json({"result":"nothing to update"});
        }
    }
    catch(error){
        return response.status(400).json({"error":error.toString()});
    }
});



module.exports = route; 
