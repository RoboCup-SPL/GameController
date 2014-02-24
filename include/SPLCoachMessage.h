#ifndef SPLCOACHMESSAGE_H
#define SPLCOACHMESSAGE_H

typedef unsigned char uint8;

static const int SPL_COACH_MESSAGE_STRUCT_VERSION = 1;
static const int SPL_COACH_MESSAGE_SIZE = 20;

struct SPLCoachMessage 
{
  char header[4];        // "SPLC"
  uint8 version;       // SPL_COACH_MESSAGE_STRUCT_VERSION
  uint8 team;          // 0 is blue 1 is red

  // buffer for message
  uint8 message[SPL_COACH_MESSAGE_SIZE];
               
  // constructor
  SPLCoachMessage()
  {
    header[0] = 'S';
    header[1] = 'P';
    header[2] = 'L';
    header[3] = 'C';
    version = SPL_COACH_MESSAGE_STRUCT_VERSION;
  }
};

#endif // SPLCOACHMESSAGE_H
