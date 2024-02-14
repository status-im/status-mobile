(ns quo.components.share.share-community-channel-qr-code.style
  (:require [quo.foundations.colors :as colors]))

(def outer-container
  {:border-radius 16
   :overflow      :hidden})

(def ^:private padding 12)

(def container-component
  {:padding-bottom   padding
   :background-color colors/white-opa-5})

(def content-container
  {:z-index            1
   :padding-horizontal padding
   :padding-top        12})

(defn qr-code-size
  [total-width]
  (- total-width (* 2 padding)))

