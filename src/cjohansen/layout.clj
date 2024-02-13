(ns cjohansen.layout)

(defn layout [& body]
  [:html
   [:head
    [:meta {:name "author" :content "Christian Johansen"}]]
   [:body
    body]])
