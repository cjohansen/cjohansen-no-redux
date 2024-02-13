(ns cjohansen.time
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(defn ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "MMMM d yyy")))
