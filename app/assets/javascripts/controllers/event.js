angular.module('cc.controllers.event',[])
.controller('CcEventCtrl', function($scope, $routeParams,$rootScope, Messages,  RtcUserMedia, RtcPeer){


  console.log('after dummy');
  $scope.member = $rootScope.dummySession;
  $scope.username = $scope.member.firstName+' '+$scope.member.lastName;

  $scope.vm= this;
  //TODO refactoring
  $scope.event = {
    '_id' : '56f3ce75a7da1a565697818d',
    'parent_id' : '56f3c5dfa7da1a565697817e',
    'gender' : 'chat_streaming',
    'date' : new Date().getTime(),
    'start_time' : '',
    'end_time' : '',
    'title' : 'Live Stream & Tchat',
    'document_id' : '56f3cd66a7da1a5656978189',
    'description' : '',
    'update_date' : 1600964443588
  };

  $scope.messages = Messages.initChat($scope.event._id);
  $rootScope.noMediaInit = false;


  $scope.membersCount = function(){
    console.log('members count', $scope.messages.members.length, Array.size($scope.messages.members));
    return $scope.messages.members.length;
  };

  $scope.pc= null;
  var pc = $scope.pc;
  $rootScope.pc = pc;

  $scope.startPeering = function(){

    var pc_config = {
      'iceServers': [
        {
          'url': 'stun:stun.l.google.com:19302'
        }
      ]
    };
    var pc_contraints ={'optional': [
        {'DtlsSrtpKeyAgreement': true}
      ]};

    var onaddstream = function(evt){
      // $scope.vm.streamUrl= URL.createObjectURL(evt.stream);
      console.log('adding remote ======>', evt.stream);
      $scope.vm.streamRemoteUrl = RtcUserMedia.getStreamUrl(evt.stream);
    };



    var onicecandidate = function(evt){
      if (evt.candidate)
        $scope.video.send(JSON.stringify({
          'candidate': evt.candidate,
          'text': 'init ice candiate'
        }));
    };

    $rootScope.pc = pc = $scope.vm.pc = new RtcPeer.PeerConnection(pc_config, pc_contraints);
    console.log('pc ===>', pc);
    pc.onnegotiationneeded = function () {
      console.log('trying negos==========> negos', pc);

      if($rootScope.noMediaInit)
        pc.createOffer(localDescCreated, onError);
    };
    pc.onaddstream = onaddstream;
    pc.onicecandidate = onicecandidate;

    var onSuccess = function(stream){
      if($scope.vm.stream) $scope.vm.stream.stop();
      $scope.vm.stream = stream;
      pc.addStream(stream);
      //if($rootScope.noMediaInit)
      $scope.vm.streamUrl = RtcUserMedia.getStreamUrl(stream);
      console.log('stream to be CREATED', stream);
    };

    var onError = function(err){
      console.log('error in video stream ====>', err);
    };

    $scope.vm.getUserMedia = function(constraints){
      console.log('medias inti');
      RtcUserMedia.getUserMedia(
        constraints,
        onSuccess,
        onError
      );
    };


    //if(!$rootScope.noMediaInit)
    $scope.vm.getUserMedia({'audio': true,'video': true});

    $scope.$on('$destroy', function(){
      if($scope.vm.stream) $scope.vm.stream.stop();
    });


    function localDescCreated(desc) {

      console.log('pc ****** loc desc', pc.localDescription, ' ====> ', desc);
      pc.setLocalDescription(desc, function () {
        $scope.video.send(JSON.stringify({
          'sdp': pc.localDescription,
          'text': 'desc'
        }));
      }, onError);
    }
  };


  $scope.startStreaming = function(){
    $scope.video = Messages.initVideo($scope.event._id, pc, $scope.startPeering);
  };
  $scope.submit = function(new_message) {
    console.log('submitting', new_message);
    if (!new_message) { return; }
    $scope.messages.send({username: $scope.username, text : new_message});
    $scope.new_message = '';
  };
});