package com.worldcup.bracket.DTO

data class WhoScoredEvents (
    val matchCentreData: MatchCentreData
)

data class MatchCentreData (
    val playerIdNameDictionary: Map<String,String>,
    val events: List<Event>
)

data class Event(
    val playerId: String,
    val qualifiers: List<Qualifiers>,
    val type: TypeOfEvent
)

data class Qualifiers(
    val type: QualifierType
)

data class QualifierType(
    val value: Int,
    val displayName: String
)

data class TypeOfEvent(
    val displayName: String
)


/* 
example response for a goal scored from outside the box (see OutOfBoxCentre)
{
  "id": 2503954635,
  "eventId": 401,
  "minute": 37,
  "second": 19,
  "teamId": 304,
  "playerId": 320834,
  "relatedEventId": 400,
  "relatedPlayerId": 313542,
  "x": 80.5,
  "y": 35,
  "expandedMinute": 37,
  "period": {
    "value": 1,
    "displayName": "FirstHalf"
  },
  "type": {
    "value": 16,
    "displayName": "Goal"
  },
  "outcomeType": {
    "value": 1,
    "displayName": "Successful"
  },
  "qualifiers": [
    {
      "type": {
        "value": 103,
        "displayName": "GoalMouthZ"
      },
      "value": "19.6"
    },
    {
      "type": {
        "value": 22,
        "displayName": "RegularPlay"
      }
    },
    {
      "type": {
        "value": 18,
        "displayName": "OutOfBoxCentre"
      }
    },
    {
      "type": {
        "value": 55,
        "displayName": "RelatedEventId"
      },
      "value": "400"
    },
    {
      "type": {
        "value": 29,
        "displayName": "Assisted"
      }
    },
    {
      "type": {
        "value": 178,
        "displayName": "StandingSave"
      }
    },
    {
      "type": {
        "value": 72,
        "displayName": "LeftFoot"
      }
    },
    {
      "type": {
        "value": 56,
        "displayName": "Zone"
      },
      "value": "Center"
    },
    {
      "type": {
        "value": 76,
        "displayName": "LowLeft"
      }
    },
    {
      "type": {
        "value": 102,
        "displayName": "GoalMouthY"
      },
      "value": "53.9"
    }
  ],
  "satisfiedEventsTypes": [
    91,
    24,
    9,
    10,
    2,
    3,
    13,
    18,
    19,
    26
  ],
  "isTouch": true,
  "goalMouthZ": 19.6,
  "goalMouthY": 53.9,
  "isGoal": true,
  "isShot": true
}



 example response for an error leading to a goal:
{
  "id": 2517342087,
  "eventId": 702,
  "minute": 60,
  "second": 27,
  "teamId": 304,
  "playerId": 91961,
  "x": 17,
  "y": 70.2,
  "expandedMinute": 63,
  "period": {
    "value": 2,
    "displayName": "SecondHalf"
  },
  "type": {
    "value": 51,
    "displayName": "Error"
  },
  "outcomeType": {
    "value": 1,
    "displayName": "Successful"
  },
  "qualifiers": [
    {
      "type": {
        "value": 170,
        "displayName": "LeadingToGoal"
      }
    }
  ],
  "satisfiedEventsTypes": [
    98
  ],
  "isTouch": false
}
*/