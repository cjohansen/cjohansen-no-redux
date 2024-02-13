(ns cjohansen.pages
  (:require [cjohansen.tech-blog :as tech-blog]))

(defn render-page [req page]
  (let [f (case (:page/kind page)
            :page.kind/frontpage tech-blog/render-frontpage
            :page.kind/tech-blog-post tech-blog/render-page)]
    (f req page)))
