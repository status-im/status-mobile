(ns status-im.contexts.communities.actions.share-community-channel.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [safe-area-top]
  {:padding-top safe-area-top})

(def header-container
  {:padding-horizontal 20
   :padding-vertical   12})

(def scan-notice
  {:color        colors/white-70-blur
   :margin-top   16
   :margin-left  :auto
   :margin-right :auto})

(def qr-code-wrapper
  {:padding-horizontal 20
   :margin-top         8
   :align-items        :center})

