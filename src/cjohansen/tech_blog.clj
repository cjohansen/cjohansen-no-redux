(ns cjohansen.tech-blog
  (:require [cjohansen.layout :as layout]
            [cjohansen.time :as time]
            [datomic-type-extensions.api :as d]
            [powerpack.markdown :as md]
            [ui.elements :as e]))

(defn get-blog-posts [db]
  (->> (d/q '[:find ?e ?p
              :in $
              :where
              [?e :tech-blog/published ?p]]
            db)
       (sort-by second)
       reverse
       (map #(d/entity db (first %)))))

(defn prepare-teaser [{:tech-blog/keys [short-title description published updated tags] :page/keys [title uri]}]
  {:title (or short-title title)
   :published (time/ymd published)
   :updated (some-> updated time/ymd)
   :url uri
   :description (md/render-html description)
   :kind :article
   :tags (->> tags
              (sort-by :tag/name)
              (map (fn [tag] {:title (:tag/name tag)
                              :url (:page/uri tag)})))})

(defn add-byline [{:tech-blog/keys [published updated]} sections]
  (let [sections (into [] sections)
        ks (cond
             (and published updated)
             {:updated (time/ymd updated)
              :published (time/ymd published)}
             published {:published (time/ymd published)})]
    (into [(merge (into {} (first sections)) ks)]
          (rest sections))))

(defn prepare-post-section [{:section/keys [body title sub-title theme kind] :as section}]
  (e/section
   {:title title
    :sub-title sub-title
    :heading-level 2
    :meta (when (or (:published section) (:updated section))
            {:published (:published section) :updated (:updated section)})
    :content (md/render-html body)
    :theme theme
    :kind kind}))

(defn render-page [_ctx blog-post]
  (layout/layout
   {:title (:page/title blog-post)}
   (e/simple-header)
   (->> (:sectioned/sections blog-post)
        (sort-by :section/number)
        (add-byline blog-post)
        (map prepare-post-section))
   (e/footer)))

(defn render-frontpage [ctx page]
  (layout/layout
   {:title "Christian Johansen"}
   (e/header)
   (when-let [description (:page/body page)]
     (e/section {:content (md/render-html description)}))
   (e/teaser-section
    {:title "Blog posts"
     :teasers (map prepare-teaser (get-blog-posts (:app/db ctx)))})
   (e/footer)))

(defn render-tag-page [_ tag]
  (layout/layout
   {:title (:tag/name tag)}
   [:div
    (e/simple-header)
    (e/teaser-section
     {:title (:tag/name tag)
      :teasers (->> tag
                    :tech-blog/_tags
                    (filter :tech-blog/published)
                    (sort-by :tech-blog/published)
                    reverse
                    (map prepare-teaser))})
    (e/footer)]))

(comment

  (def conn (:datomic/conn (powerpack.dev/get-app)))
  (def db (d/db conn))

  (into {} (first (get-blog-posts db)))
  )
