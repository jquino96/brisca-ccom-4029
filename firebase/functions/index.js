const functions = require('firebase-functions');
const admin = require('firebase-admin');
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
admin.initializeApp();

exports.playerCreated = functions.firestore
    .document('player/{playerID}')
    .onCreate( (event, context) => {
        const playerID = context.params.playerID;
        const player = event.data();
        const db = admin.firestore();
        return db.runTransaction( trs => {
            return trs.get(db.collection('room')
                .where('full', '==', false)
                .where('max_size', '==', player.room_size)
                .limit(1))
                .then( result => {
                    var roomID;
                    var dealer;
                    if (result.size === 1) {
                        // Get room document snapshot
                        const roomSnap = result.docs[0];
                        const room = roomSnap.data();
                        const players = room.players.concat([playerID]);
                        console.log(players.concat(room.players));
                        const size = room.size + 1;
                        const full = (size === room.max_size);
                        const newRoom = {players, size, full};
                        // Set document to new room data
                        trs.update(roomSnap.ref, newRoom);
                        roomID = roomSnap.id;
                        //
                        event.ref.update({dealer: false});
                    } else {
                        const players = [playerID];
                        const roomRef = db.collection('room').doc();
                        trs.set(roomRef, {players, size: 1, max_size: player.room_size, full: false});
                        roomID = roomRef.id;
                        event.ref.update({dealer: true});
                    }
                    return trs.update(db.collection('player').doc(playerID), {room: roomID});
                });
        });
    });

exports.roomUpdate = functions.firestore
    .document('room/{roomID}')
    .onUpdate((change, context) => {
        const roomID = context.params.roomID;
        const oldData = change.before.data();
        const newData = change.after.data();
        const db = admin.firestore();
        if (newData.full === true && oldData.full === false) {
            const players = newData.players;
            const hands = {};
            newData.players.forEach( (player, i) =>
                hands[player] = []
            );
            const gameRef = db.collection('game').doc();
            gameRef.set({room: roomID,
                players,
                turn: 0,
                played: [],
                deck: [],
                hands
            });
            const gameID = gameRef.id;
            const dealerRef = db.collection('player').doc(newData.players[0]);
            dealerRef.update({dealer: true});
            return change.after.ref.update({game: gameID});
        }
        return null;
    });