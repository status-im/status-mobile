(ns status-im.ui.screens.activity-center.sheet.contact-verification
  (:require [status-im.ui.screens.activity-center.notification.contact-verification.view :as contact-verification]))

(defn- reply-view
  [{:keys [notification replying?]}]
  [contact-verification/view notification {:replying? replying?}])

(def reply
  {:content reply-view})
