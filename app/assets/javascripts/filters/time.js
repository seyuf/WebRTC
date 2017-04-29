
//TODO refac use instead angular's moment
angular.module('cc.filters.time',[])
.filter('timeago', function() {
  return function(date) {
    return moment(date).fromNow();
  };
})
.filter('calendar', function() {
  return function(date) {
    return moment(date).calendar();
   };
});