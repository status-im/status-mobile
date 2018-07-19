(ns status-im.ui.screens.add-new.new-public-chat.subs
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.add-new.new-public-chat.db :as db]
            [cljs.spec.alpha :as spec]))

(re-frame/reg-sub
 :new-topic-error-message
 :<- [:get :public-group-topic]
 (fn [topic]
   (when-not (spec/valid? ::db/topic topic)
     (i18n/label :topic-name-error))))
