(ns status-im.ui.screens.privacy-policy.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]))

(def ^:const privacy-policy-link "https://www.iubenda.com/privacy-policy/45710059")

(re-frame/reg-fx
 ::open-privacy-policy
 (fn [] (.openURL react/linking privacy-policy-link)))

(handlers/register-handler-fx
 :open-privacy-policy-link
 (fn [] {::open-privacy-policy nil}))
