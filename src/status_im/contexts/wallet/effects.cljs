(ns status-im.contexts.wallet.effects 
  (:require    [re-frame.core :as rf]
               [react-native.share :as share]))

(rf/reg-fx :effects.share/open
           (fn [content]
             (share/open content)))