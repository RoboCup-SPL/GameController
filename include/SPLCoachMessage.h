#ifndef SPLCOACHMESSAGE_H
#define SPLCOACHMESSAGE_H

#include <stdint.h>

static const int SPL_COACH_MESSAGE_STRUCT_VERSION = 2;
static const int SPL_COACH_MESSAGE_SIZE = 40;

struct SPLCoachMessage 
{
  char header[4];        // "SPLC"
  uint8_t version;       // SPL_COACH_MESSAGE_STRUCT_VERSION
  uint8_t team;          // 0 is blue 1 red

  // buffer for message
  uint8_t message[SPL_COACH_MESSAGE_SIZE];

#ifdef __cplusplus
  // constructor
  SPLCoachMessage()
  {
    header[0] = 'S';
    header[1] = 'P';
    header[2] = 'L';
    header[3] = 'C';
    version = SPL_COACH_MESSAGE_STRUCT_VERSION;
  }
#endif
};

#endif // SPLCOACHMESSAGE_H
