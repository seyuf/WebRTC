dependencies = [
  'ngRoute',
  'cc.controllers.event',
  'cc.services.chat',
  'cc.directive.ngEnter',
  'cc.filters.time',
  'ui.bootstrap',
  'ngLodash',
  'ngWebSocket',
  'http-auth-interceptor',
  'ac.angular-webrtc',
  'angular-growl'
];

angular.module('WebRTC',dependencies)
  .config(['$routeProvider',function($routeProvider){
    $routeProvider
      .when('/event/:id',{
        templateUrl:'/assets/partials/event.html',
        controller:'CcEventCtrl'
      })
      .otherwise({
        redirectTo:'/event/test'
      });

  }])
  .config(function($httpProvider) {
    $httpProvider.defaults.withCredentials = true;
  })
  .config(['growlProvider', function(growlProvider) {
    growlProvider.globalTimeToLive(3000);
  }])
  .run(function($rootScope, $route, $location, growl){
  $rootScope.logged = true;
  $rootScope.logErrorMsg = false;
  $rootScope.dummySession = {
    'role': 'Administrator',
    'email': 'test@test.loc',
    'firstName': 'Test',
    'lastName': new Date().getTime()+'',
    'password': 'Well Tried friend',
    'avatar_path': ''
  };
  $rootScope.addSpecialWarnMessage = function(message) {
    if(message.type === "WARNING")
      growl.warning(message.text);
    else if(message.type === "INFO")
      growl.info(message.text);
    else if(message.type === "ERROR")
      growl.error(message.text);
    else if(message.type === "SUCCESS")
      growl.success(message.text);
  };
});
