(ns status-im.privacy-policy.core
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]))

(def ^:const privacy-policy-link "https://www.iubenda.com/privacy-policy/45710059")

(defn open-privacy-policy-link! []
  (.openURL react/linking privacy-policy-link))

(re-frame/reg-fx
 :privacy-policy/open-privacy-policy-link
 (fn []
   (open-privacy-policy-link!)))

(defn open-privacy-policy-link []
  {:privacy-policy/open-privacy-policy-link nil})
