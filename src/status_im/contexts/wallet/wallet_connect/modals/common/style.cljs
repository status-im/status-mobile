(ns status-im.contexts.wallet.wallet-connect.modals.common.style
  (:require [quo.foundations.colors :as colors]
            [status-im.constants :as constants]))

(defn container
  [bottom]
  {:position    :absolute
   :bottom      bottom
   :top         0
   :left        0
   :right       0
   :padding-top constants/sheet-screen-handle-height})

(def data-content-container
  {:flex               1
   :padding-horizontal 20})

(def sign-message-content-container
  (merge data-content-container
         {:margin-bottom 10.5}))

(def data-item
  {:flex             1
   :background-color :transparent
   :margin-bottom    8
   :margin-top       2})

(def data-item-container
  {:padding       10
   :margin-top    10.5
   :margin-bottom 0
   :border-width  1
   :border-color  colors/neutral-10
   :border-radius 16})
