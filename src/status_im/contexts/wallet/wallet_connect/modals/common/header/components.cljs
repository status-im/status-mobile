(ns status-im.contexts.wallet.wallet-connect.modals.common.header.components
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.wallet-connect.modals.common.header.style :as style]))

(defn title-text
  [{:keys [text]}]
  [:<>
   (->> (string/split text #" ")
        (map
         (fn [word]
           ^{:key word}
           [rn/view {:style style/word-container}
            [quo/text
             {:size   :heading-1
              :weight :semi-bold}
             (str " " word)]]))
        (doall))])

(defn title-summary
  [summary-tag-props]
  [rn/view {:style style/dapp-container}
   [quo/summary-tag summary-tag-props]])

(defn title-container
  [& children]
  (into [rn/view {:style style/header-container}] children))
