(ns status-im.contexts.wallet.create-account.edit-derivation-path.style
  (:require [quo.foundations.colors :as colors]))

(defn screen
  [top]
  {:flex       1
   :margin-top top})

(def header
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     8})

(def tag
  {:padding-horizontal 20
   :flex-direction     :row})

(def input-container
  {:padding-horizontal 20
   :padding-top        20})

(defn save-button-container
  [bottom]
  {:flex            1
   :justify-content :flex-end
   :padding-bottom  bottom})

(def revealed-address-container
  {:padding-horizontal 20
   :padding-top        24})

(defn revealed-address
  [state theme]
  {:border-width       1
   :border-color       (if (= state :scanning)
                         (colors/theme-colors colors/neutral-20 colors/neutral-70 theme)
                         (colors/resolve-color (if (= state :has-activity) :success :warning) theme 40))
   :border-style       :dashed
   :border-radius      16
   :padding-horizontal 12
   :padding-vertical   7})

(def info
  {:margin-vertical 9
   :padding-left    2})

(def temporal-placeholder
  {:height           94
   :background-color colors/danger-50
   :align-items      :center
   :justify-content  :center})
