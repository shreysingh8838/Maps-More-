const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');
const cors = require('cors');
const app = express();
app.use(cors({ origin: true }));

var serviceAccount = require("./permissions.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://fir-api-9a206..firebaseio.com"
});
const db = admin.firestore();

app.get('/', (req, res) => {
  return res.status(200).send('Hello this is a normal http get request from shrey firebase for checking purpose');
});

// function url - 
// https://us-central1-roadsafety-d90af.cloudfunctions.net/app

// add data to the main table
app.post('/api/add',(req,res)=>{
    (async () => {
        console.log(req.body);
        try {
            await db.collection('items').add({
                name : req.body.name,
                lat : req.body.lat,
                long : req.body.long,
                Acc_per_year : req.body.Acc_per_year,
                Speed_limit : req.body.Speed_limit,
                description : req.body.description
            });
            res.write('Address added');
            return res.status(200).send();
        } catch (error) {
          console.log(error);
          return res.status(500).send(error);
        }
    })();
});

// sample link : https://us-central1-roadsafety-d90af.cloudfunctions.net/app/useraddtoapi/add?name=burari&lat=212&long=1221&Acc_per_year=11&Speed_limit=22&description=yes&time=1221648&vehicle_number=DL992
// sample link : http://localhost:5001/roadsafety-d90af/us-central1/app/useraddtoapi/add?name=burari&lat=212&long=1221&Acc_per_year=11&Speed_limit=22&description=yes&time=1221648&vehicle_number=DL992
// add data to user table
// using query parameters
app.post('/useraddtoapi/add',(req,res)=>{
    (async () => {
        console.log(req.query);
        try {
            if(req.query){
                await db.collection('useraddtoapi').add({
                    name : req.query.name,
                    lat : req.query.lat,
                    long : req.query.long,
                    Acc_per_year : req.query.Acc_per_year,
                    Speed_limit : req.query.Speed_limit,
                    description : req.query.description,
                    time : req.query.time
                });
            }
            res.write('Address added to user table');
            return res.status(200).send();
        } catch (error) {
          console.log(error);
          return res.status(500).send(error);
        }
    })();
});

// read item
// sample link format - http://localhost:5001/roadsafety-d90af/us-central1/app/api/readone?item_id=zXatdRboEyXyyPDHMC78
// app.get('/api/readone', (req, res) => {
//     (async () => {
//         try {
//             const document = db.collection('items').doc(req.query.item_id);
//             let item = await document.get();
//             let response = item.data();
//             return res.status(200).send(response);
//         } catch (error) {
//             console.log(error);
//             return res.status(500).send(error);
//         }
//     })();
// });

// read all from only main collection
app.get('/api/read', (req, res) => {
    (async () => {
        try {
            let query = db.collection('items');
            let response = [];
            await query.get().then(querySnapshot => {
            let docs = querySnapshot.docs;
            for (let doc of docs) {
                const selectedItem = {
                    name: doc.data().name,
                    long: doc.data().long,
                    lat: doc.data().lat,
                    Acc_per_year : doc.data().Acc_per_year,
                    Speed_limit : doc.data().Speed_limit,
                    description : doc.data().description,
                };
                response.push(selectedItem);
            }
            });
            return res.status(200).send(response);
        } catch (error) {
            console.log(error);
            return res.status(500).send(error);
        }
    })();
});

// link : https://us-central1-roadsafety-d90af.cloudfunctions.net/app/useraddtoapi/read
// read from only users collection
app.get('/useraddtoapi/read', (req, res) => {
    (async () => {
        try {
            let query = db.collection('useraddtoapi');
            let response = [];
            await query.get().then(querySnapshot => {
            let docs = querySnapshot.docs;
            for (let doc of docs) {
                const selectedItem = {
                    name: doc.data().name,
                    long: doc.data().long,
                    lat: doc.data().lat,
                    Acc_per_year : doc.data().Acc_per_year,
                    Speed_limit : doc.data().Speed_limit,
                    description : doc.data().description,
                    time : doc.data().time,
                    vehicle_number : doc.data().vehicle_number
                };
                response.push(selectedItem);
            }
            });
            return res.status(200).send(response);
        } catch (error) {
            console.log(error);
            return res.status(500).send(error);
        }
    })();
});

exports.app = functions.https.onRequest(app);