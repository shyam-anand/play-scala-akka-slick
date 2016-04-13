package models

import java.sql.Timestamp

/**
  * Created by shyam on 30/03/16.
  */

case class WSLog(id: Long, message: String, userID: String, response_status: String, refId: String, attempt: Int, timeStamp: Timestamp)

