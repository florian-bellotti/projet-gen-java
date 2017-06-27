'use strict';

/**********************************************************************
 * Angular Application
 **********************************************************************/
var app = angular.module('app', ['ngResource', 'ngRoute'])
  .config(function($routeProvider, $locationProvider, $httpProvider) {
    //================================================
    // Define all the routes
    //================================================
    $routeProvider
      .when('/decodedFiles', {
        templateUrl: 'views/decodedFiles.html',
        controller: 'DecodedFilesCtrl'
      })
      .when('/dictionary', {
        templateUrl: 'views/dictionary.html',
        controller: 'DictionaryCtrl'
      })
      .otherwise({
        redirectTo: '/dictionary'
      });
  })
  .run(function($rootScope, $http){});

/**********************************************************************
 * DecodeFiles controller
 **********************************************************************/
app.controller('DecodedFilesCtrl', function($scope, $rootScope, $http, $location) {
  $scope.decodedFiles = null;

  $scope.getDecodedFiles = function() {
    $http.get('/server/remote/services/decodedFiles')
    .success(function(response){
      console.log(response);
      $scope.decodedFiles = response.decodedFile;
    })
    .error(function(reponse){
      console.log(response);
    });
  }

  $scope.getDecodedFiles();
});

/**********************************************************************
 * Dictionary controller
 **********************************************************************/
app.controller('DictionaryCtrl', function($scope, $rootScope, $http, $location) {
    $scope.words = null;
    var lastResearch = "";
    $scope.message = "";
    $scope.load = null;

    $scope.research = function(contains) {
        $scope.message= null;
        $scope.load = true;
        if (contains == null || contains == undefined || contains == "") {
            $scope.words = null;
            $scope.message = "Entrez au minimun une lettre";
            $scope.load = false;
            return;
        }

        lastResearch = contains;
        $http.get('/server/remote/services/words/?contains='+ contains)
            .success(function(response){
                console.log(response);
                $scope.words = response.word;
                $scope.load = false;
            })
            .error(function(reponse){
                console.log(response);
                $scope.load = false;
            });
    }

    $scope.add = function(object) {
        $scope.words = null;
        $scope.message= null;
        $scope.load = true;
        $http.post('/server/remote/services/words', {word: {label : object}})
            .success(function(response){
                console.log(response);
                $scope.research(object);
                $scope.load = false;
            })
            .error(function(reponse){
                console.log(response);
                $scope.research(object);
                $scope.load = false;
            });
    }

    $scope.delete = function(id) {
        $scope.words = null;
        $scope.message= null;
        $scope.load = true;
        $http.delete('/server/remote/services/words/'+id)
            .success(function(response){
                console.log(response);
                $scope.research(lastResearch);
                $scope.load = false;
            })
            .error(function(reponse){
                console.log(response);
                $scope.research(lastResearch);
                $scope.load = false;
            });
    }
});
