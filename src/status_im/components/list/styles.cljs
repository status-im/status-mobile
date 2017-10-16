(ns status-im.components.list.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :as styles]
            [status-im.utils.platform :as platform]))

(def item
  {:flex-direction :row})

(def item-text-view
  {:flex           1
   :flex-direction :column})

(def primary-text-base
  {:font-size   17
   :color       styles/color-black})

(def primary-text
  (merge primary-text-base
         {:padding-top (if platform/ios? 13 14)}))

(def primary-text-only
  (merge primary-text-base
         {:padding-vertical 22}))

(def secondary-text
  {:font-size   16
   :color       styles/color-gray4
   :padding-top 4})

(def item-image
  {:width  40
   :height 40})

(def item-icon
  {:width  24
   :height 24})

(def left-item-wrapper
  {:margin 14})

(def content-item-wrapper
  {:flex         1
   :margin-right 16})

(def right-item-wrapper
  {:margin-right 16})

(def base-separator
  {:height           1
   :background-color styles/color-gray5
   :opacity          0.5})

(def separator
  (merge
    base-separator
    {:margin-left   70}))

(defstyle section-separator
  (merge base-separator
         {:android {:margin-top 12}
          :ios     {:margin-top 16}}))

(defstyle section-header
  {:font-size       14
   :color           styles/color-gray4
   :margin-left     16
   :android         {:margin-top    11
                     :margin-bottom 3}
   :ios             {:margin-top    10
                     :margin-bottom 2}})
