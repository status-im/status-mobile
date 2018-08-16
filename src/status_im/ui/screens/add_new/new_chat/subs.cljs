(ns status-im.ui.screens.add-new.new-chat.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :new-identity-error
                  (fn [db _]
                    (get db :contacts/new-identity-error nil)))
