(ns cjohansen.layout)

(defn layout [{:keys [title]} & body]
  [:html
   [:head
    [:meta {:name "author" :content "Christian Johansen"}]
    [:link {:href "/atom.xml" :rel "alternate" :title "cjohansen.no" :type "application/atom+xml"}]
    [:title title]]
   [:body
    body]])
