(ns legacy.status-im.ui.components.list.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.utils.styles :as styles]))

(def base-separator
  {:height           1
   :background-color colors/black-transparent})

(def separator
  (merge
   base-separator
   {:margin-left 64}))

(styles/def section-header
  {:font-size   14
   :color       colors/gray
   :margin-left 16
   :margin-top  16
   :android     {:margin-bottom 3}
   :ios         {:margin-bottom 10}})

(def section-header-container {})
