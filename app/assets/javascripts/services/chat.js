angular.module('cc.services.chat',[])
.factory('Messages', function($websocket, $location,  lodash, RtcPeer, $rootScope) {
  var ws2 = '';
  var socket = {
    initChat: function(eventRoom){
      var domain = location.host;
      var user =$rootScope.dummySession;
      //var username = user.firstName+' '+user.lastName;

      var N = 6;
      var username = Array(N+1).join((Math.random().toString(36)+ new Date().getTime() +'').slice(2, 18)).slice(0, N);
      if('URLSearchParams' in window){
        var urlParams = new URLSearchParams(location.search);
        var curMarker = urlParams.get('marker');
        username =  curMarker ? curMarker: username;
      }

      //console.log('username', username);
      if(user === null || username == ' '){
        return;
      }

      var ws = $websocket('ws://'+domain+'/room/chat/'+eventRoom+'/'+ username);

      var collection = [];
      var members = [];

      ws.onMessage(function(event) {
        var res;
        try {
          res = JSON.parse(event.data);
        } catch(e) {
          res = {'username': 'anonymous', 'message': event.data};
        }

        var meCheck  = username === res.user? true: false;
        var tmpmembers = res.members;
        //members = lodash.cloneDeep(res.members);
        //console.log('res!====>', res,event.timeStamp, angular.copy(res.members), members);
        /*
        res.members.forEach(function(item){
          members.push(item);
        });
        */

        members = lodash.cloneDeep(res.members);

        members = lodash.uniq(members);
        //console.log('res!====> Post',  members);
        /*

        */

        if(collection.length <= 0){

          res.collection.forEach(function (curElem) {
            collection.push({
              user: curElem.user,
              text: curElem.text,
              me: (username === curElem.user),
              timeStamp: curElem.timeStamp
            });
          });
        } else {
          if(res.message) {
            collection.push({
              user: res.user,
              text: res.message,
              me: meCheck,
              timeStamp: res.time
            });
          }
        }
      });

      ws.onError(function(event) {
        console.log('connection Error', event);
        ws.close();
      });

      ws.onClose(function(event) {
        console.log('connection closed', event);
        window.location.reload();
        ws.close();
      });

      ws.onOpen(function() {
        console.log('connection open');
        //var openJson = {username: username, text: 'Greetings'};
        //ws.send(JSON.stringify(openJson));
      });

      var returnObj = {
        collection: collection,
        members : members,
        status: function() {
          return ws.readyState;
        },
        send: function(message) {
          if (angular.isString(message)) {
            ws.send(message);
          }
          else if (angular.isObject(message)) {
            ws.send(JSON.stringify(message));
          }
        },
        membersCount: function(){
          return members.length;
        },
        membersInfo: function(){
          return members;
        }

      };

      return returnObj;

    },
    initVideo: function(eventRoom, pc, start){
      var domain = location.host;
      var user = $rootScope.dummySession;
      var username = user.firstName+' '+user.lastName;

      if(user === null || username == ' '){
        return;
      }

      ws2 = $websocket('ws://'+domain+'/room/video/'+eventRoom+'/'+ username);
      var collection = [];
      var members = [];

      ws2.onMessage(function(event) {
        var res;
        try {
          res = JSON.parse(event.data);
        } catch(e) {
          res = {'username': 'anonymous', 'message': event.data};
        }
        //console.log('pc ===>', pc);
        if(res.members.length >1) $rootScope.noMediaInit = true;

        function onError(err){ console.log('error==>', err);}
        function localDescCreated(desc) {
          //console.log('pc ****** loc desc', pc.localDescription, ' ====> ', desc);
          pc.setLocalDescription(desc, function () {
            console.log('send anwser description');
            ws2.send(JSON.stringify({
              'sdp': pc.localDescription,
              'text': 'desc local'
            }));
          }, onError);
        }
        if (!pc) {
          start();
        }

        pc = $rootScope.pc;
        //console.log('pc passed===>', pc, $rootScope.pc);
        var message = JSON.parse(event.data);
        message = message.data;
        console.log('message===>', message, event);
        if (message.sdp && (res.user !== username)){
          console.log('new sdp!!! =======>', message.sdp, localDescCreated);
          pc.setRemoteDescription(new RtcPeer.SessionDescription(message.sdp), function () {
            // if we received an offer, we need to answer
            //console.log('new offer!!! =======>', res.text, message.sdp);
            if (pc.remoteDescription.type == 'offer'){
              pc.createAnswer(localDescCreated, function(err){ console.log('err at create anws2 offer', err);});
            }
          }, function(err){ console.log('err at setting remote Description', err);});
        }
        else if(message.candidate && (res.user !== username)){
          console.log('new candidate ===>', message.candidate);
          pc.addIceCandidate(new RtcPeer.IceCandidate(message.candidate));
        }
        else{
          console.log('no init done');
        }

        var meCheck  = username === res.user? true: false;
        var tmpmembers = res.members;
        res.members.forEach(function(item){
          members.push(item);
        });
        members = lodash.uniq(members);
        collection.push({
          user: res.user,
          text: res.message,
          me: meCheck,
          timeStamp: event.timeStamp
        });
      });


      ws2.onError(function(event) {
        console.log('connection Error', event);
      });

      ws2.onClose(function(event) {
        console.log('connection closed', event);
      });

      ws2.onOpen(function() {
        console.log('connection open');
        var openJson = {username: username, text: 'Hi everyone! '};
        ws2.send(JSON.stringify(openJson));
      });

      return {
        collection: collection,
        members : members,
        status: function() {
          return ws2.readyState;
        },
        send: function(message) {
          if (angular.isString(message)) {
            ws2.send(message);
          }
          else if (angular.isObject(message)) {
            ws2.send(JSON.stringify(message));
          }
        }

      };
    }
  };
  return socket;
});
