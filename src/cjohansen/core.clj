(ns cjohansen.core
  (:require [cjohansen.ingest :as ingest]
            [cjohansen.pages :as pages]
            [powerpack.highlight :as highlight]))

(defn create-app []
  (-> {:site/base-url "https://cjohansen.no"
       :site/default-locale :no
       :site/title "Christian Johansen"

       :powerpack/build-dir "target"
       :powerpack/content-dir "content"
       :powerpack/source-dirs ["src" "dev"]
       :powerpack/resource-dirs ["resources"]
       :powerpack/log-level :debug
       :datomic/uri "datomic:mem://cjohansen"
       :datomic/schema-file "resources/schema.edn"
       :optimus/assets [{:public-dir "public"
                         :paths [#"/*.(png|ico|jpg|svg)"
                                 #"/images/*.*"
                                 #"/fonts/*"]}]
       :optimus/bundles {"styles.css"
                         {:public-dir "public"
                          :paths ["/css/cjohansen.css"]}}

       :powerpack/port 4040

       :powerpack/create-ingest-tx #'ingest/create-tx
       :powerpack/render-page #'pages/render-page
       :powerpack/on-ingested #'ingest/on-ingested}
      highlight/install))
