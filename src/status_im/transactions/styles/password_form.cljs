(ns status-im.transactions.styles.password-form
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.components.styles :as st]))

(defnstyle password-container [error?]
  {:margin-bottom (if error? 42 24)
   :padding-left  16
   :ios           {:border-top-width 1
                   :border-top-color st/color-white-transparent-2}})

(def password-title
  {:color         st/color-white
   :font-size     15
   :margin-top    16
   :margin-bottom 12})

(def password-input-wrapper
  {:position       :relative
   :height         52
   :padding-top    0
   :padding-bottom 0
   :margin-bottom  0})

(defstyle password-input
  {:color        :white
   :height       52
   :padding-left 0
   :padding-top  24
   :android      {:font-size 16}
   :ios          {:font-size 17}})
