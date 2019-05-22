(ns status-im.privacy-policy.core
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.utils.fx :as fx]))

(def ^:const privacy-policy-link "https://www.iubenda.com/privacy-policy/45710059")

(defn open-privacy-policy-link! []
  (.openURL (react/linking) privacy-policy-link))

(re-frame/reg-fx
 :privacy-policy/open-privacy-policy-link
 (fn []
   (open-privacy-policy-link!)))

(fx/defn open-privacy-policy-link [_]
  {:privacy-policy/open-privacy-policy-link nil})
