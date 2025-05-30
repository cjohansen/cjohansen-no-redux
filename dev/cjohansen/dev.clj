(ns cjohansen.dev
  (:require [cjohansen.core :as cjohansen]
            [dataspex.core :as dataspex]
            [powerpack.dev :as dev :refer [reset]]
            [powerpack.export :as export]))

(defmethod dev/configure! :default []
  (cjohansen/create-app))

(defn start []
  (set! *print-namespace-maps* false)
  (dev/start))

(comment

  (start)
  (reset)

  (export/export (cjohansen/create-app))

  (dev/get-app)
  (dataspex/inspect "DB" (:datomic/conn (dev/get-app)))

  )
