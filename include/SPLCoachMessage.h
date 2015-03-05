#ifndef SPLCOACHMESSAGE_H
#define SPLCOACHMESSAGE_H

#include <stdint.h>
#ifdef __cplusplus
#include <cstring>
#endif

#define SPL_COACH_MESSAGE_PORT           3839

#define SPL_COACH_MESSAGE_STRUCT_HEADER  "SPLC"
#define SPL_COACH_MESSAGE_STRUCT_VERSION 3
#define SPL_COACH_MESSAGE_SIZE           81

struct SPLCoachMessage 
{
  char header[4];        // "SPLC"
  uint8_t version;       // SPL_COACH_MESSAGE_STRUCT_VERSION
  uint8_t team;          // team number
  uint8_t sequence;      // sequence number of this message

  // buffer for message
  uint8_t message[SPL_COACH_MESSAGE_SIZE];

#ifdef __cplusplus
  // constructor
  SPLCoachMessage() : version(SPL_COACH_MESSAGE_STRUCT_VERSION)
  {
    std::memcpy(header, SPL_COACH_MESSAGE_STRUCT_HEADER, sizeof(header));
  }
#endif
};

#endif // SPLCOACHMESSAGE_H
