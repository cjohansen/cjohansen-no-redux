(ns cjohansen.export
  (:require [cjohansen.core :as cjohansen]
            [clojure.java.io :as io]
            [powerpack.export :as export]))

(def opt
  {:link-ok?
   (fn [_ _ {:keys [href]}]
     (.exists (io/file (io/resource (str "public" href)))))})

(defn ^:export export [& _args]
  (set! *print-namespace-maps* false)
  (export/export! (cjohansen/create-app) opt))

(comment

  (set! *print-namespace-maps* false)
  (export/export (cjohansen/create-app) opt)

)
