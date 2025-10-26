# MTG League Manager

### Objectives
* To manage Magic The Gathering leagues, tracking player stats, games, and standings.
* Provide an intuitive interface for league organizers and players to view and manage league data.
* Support multiple leagues and formats.
* Enable easy enrollment and tracking of player performance.
* Facilitate reporting and analytics on league performance.

### Planning
* Define data models for Players, Games, Leagues, and Stats.
* Set up a backend server to handle data storage and retrieval.
* Create frontend components for user interaction.
* Implement authentication and authorization for league organizers and players.
* Test the application thoroughly to ensure reliability and usability.
* Deploy the application and monitor for issues.
* Gather user feedback for future improvements.
* Plan for regular updates and feature additions based on user needs.


### History

This project started as a simple tool to track an MTG Commander League standings. 
The next step, is to evolve it into a more comprehensive league management system, incorporating user feedback and expanding its feature set to accommodate various league formats and player needs.


# Brainstorming Area

### To-Do

* *Highest & Bugs*
  * Login
  * Multiple Leagues
  * League Enrollment
  * Error Messages
  * final/topX logic

* *High*
  * Mapper
  * Change incorrect GETs to DELETE/POST/UPDATE
  * landing page passa a ser so os pontos
  * Include penalty count/rounds not played

* *Low*
  * Track Upgrades
  * Payment Tracker
  * Different Formats (1v1)
  * Different Types of TCGs (isn't this already done?)

* *Lowest & other Refactors*
  * class PlayerGameStats
  * GameView -> passar a ter uma class interna para player + playerid
  * sorting bug? -> Sometimes players seem to switch positions (apparently solved)
