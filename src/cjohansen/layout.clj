(ns cjohansen.layout)

(defn layout [{:keys [title]} & body]
  [:html
   [:head
    [:meta {:name "author" :content "Christian Johansen"}]
    [:title title]]
   [:body
    body]])
