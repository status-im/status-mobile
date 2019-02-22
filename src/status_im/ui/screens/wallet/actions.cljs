(ns status-im.ui.screens.wallet.actions
  (:require [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.ui.components.list-selection :as list-selection]))

(defn- share-link [browser-id]
  (let [link    (universal-links/generate-link :browse :external browser-id)
        message (i18n/label :t/share-dapp-text {:link link})]
    (list-selection/open-share {:message message})))

(defn share [browser-id]
  {:label  (i18n/label :t/share-link)
   :action #(share-link browser-id)})

(defn actions [browser-id]
  [(share browser-id)])
