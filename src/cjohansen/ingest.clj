(ns cjohansen.ingest
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [powerpack.dev :as dev]))

(defn update-in-existing [m path & args]
  (if-not (nil? (get-in m path))
    (apply update-in m path args)
    m))

(defn ingest-section [page index section]
  (-> section
      (dissoc :page/uri)
      (assoc :section/number index)
      (assoc :section/id (str (:page/uri page) "-section-" index))))

(defn get-sectioned [xs]
  (->> (rest xs)
       (map-indexed (partial ingest-section (first xs)))
       (assoc (first xs) :sectioned/sections)))

(defn ensure-open-graph-data [blog-post]
  (cond-> blog-post
    (not (:open-graph/description blog-post))
    (assoc :open-graph/description (:tech-blog/description blog-post))

    (and (not (:open-graph/image blog-post))
         (:tech-blog/image blog-post))
    (assoc :open-graph/image (:tech-blog/image blog-post))))

(defn ingest-tech-blog-post [sections]
  [(-> (get-sectioned sections)
       (assoc :page/kind :page.kind/tech-blog-post)
       (update :page/uri #(str/replace % #"^/tech" ""))
       ensure-open-graph-data
       (update-in-existing [:tech-blog/tags] #(map (fn [id] {:tag/id id}) %)))])

(defn ingest-tag [[k v]]
  {:tag/id k
   :tag/name v
   :page/uri (str "/" (str/replace (str/lower-case v) #"[^a-z0-9]+" "-") "/")
   :page/kind :page.kind/tech-tag
   :page/title v
   :open-graph/description (str "Blog posts about " v)})

(defn create-tx [file-name datas]
  (cond->> datas
    (re-find #"^tech\/" file-name)
    ingest-tech-blog-post

    (re-find #"tags\.edn" file-name)
    (map ingest-tag)))

(defn on-ingested [powerpack res]
  )

(comment

  (def conn (:datomic/conn (dev/get-app)))
  (def db (d/db conn))

  (d/q '[:find ?e
         :where
         [?e :page/uri "/"]]
       db)
  (into {} (d/entity db 17592186045472))
)
