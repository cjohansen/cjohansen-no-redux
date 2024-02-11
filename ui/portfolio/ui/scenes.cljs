(ns ^:figwheel-hooks ui.scenes
  (:require [portfolio.ui :as ui]
            [ui.color-scenes]
            [ui.elements-scenes]
            [ui.highlight-scenes]
            [ui.sections-scenes]))

:ui.color-scenes/keep
:ui.elements-scenes/keep
:ui.highlight-scenes/keep
:ui.sections-scenes/keep

(defonce app
  (ui/start!
   {:config {:css-paths ["/css/cjohansen.css"]}}))
