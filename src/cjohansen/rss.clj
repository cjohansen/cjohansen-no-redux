(ns cjohansen.rss
  (:require [cjohansen.tech-blog :as tech-blog]
            [clojure.data.xml :as xml]
            [hiccup.core :refer [html]]
            [powerpack.markdown :as md])
  (:import [java.time ZoneId]
           [java.time.format DateTimeFormatter]))

(defn url [post]
  (str "https://cjohansen.no" (:page/uri post)))

(defn time-str [ldt]
  #_(str (.toOffsetDateTime
        (.atZone ldt (ZoneId/of "Europe/Oslo"))))
  (.format (.atZone ldt (ZoneId/of "Europe/Oslo")) DateTimeFormatter/ISO_OFFSET_DATE_TIME))

(defn entry [post]
  [:entry
   [:title (:page/title post)]
   [:updated (time-str (:tech-blog/published post))]
   [:author [:name "Christian Johansen"]]
  [:link {:href (url post)}]
   [:id (str "urn:cjohansen.no:feed:post:" (:page/uri post))]
   [:content {:type "html"}
    (html
        [:div
         [:div (md/render-html (:tech-blog/description post))]
         [:p [:a {:href (url post)}
              "Read article"]]])]])

(defn atom-xml [blog-posts]
  (xml/emit-str
   (xml/sexp-as-element
    [:feed {:xmlns "http://www.w3.org/2005/Atom"
            :xmlns:media "http://search.yahoo.com/mrss/"}
     [:id "urn:cjohansen.no:feed"]
     [:updated (time-str (:tech-blog/published (first blog-posts)))]
     [:title {:type "text"} "cjohansen.no"]
     [:link {:rel "self" :href "https://cjohansen.no/atom.xml"}]
     (map entry blog-posts)])))

(defn blog-post-feed [ctx _]
  {:status 200
   :headers {"Content-Type" "application/atom+xml"}
   :body (atom-xml (tech-blog/get-blog-posts (:app/db ctx)))})
